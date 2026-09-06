package com.notrash.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

object NotrashConfig {
    const val PREFS_NAME = "notrash_config"
    const val AUTHORITY = "com.notrash.provider"
    const val KEY_CAMERA_SOUND_UNLOCK = "camera_sound_unlock"
    const val KEY_ESSENTIAL_VOICE_UNLOCK = "essential_voice_unlock"
    const val DEFAULT_CAMERA_SOUND_UNLOCK = false
    const val DEFAULT_ESSENTIAL_VOICE_UNLOCK = false
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_THEME_MODE = "theme_mode"

    var highContrastEnabled by mutableStateOf(true)
        private set
    var themeMode by mutableStateOf("system")
        private set

    var isModuleActive by mutableStateOf(false)
        private set
    var settingsSynced by mutableStateOf(false)
        private set
    var systemHookCurrent by mutableStateOf<Boolean?>(null)
        private set
    var frameworkDescription by mutableStateOf<String?>(null)
        private set
    private var service: XposedService? = null
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        highContrastEnabled = getPrefs(app).getBoolean(KEY_HIGH_CONTRAST, true)
        themeMode = getPrefs(app).getString(KEY_THEME_MODE, "system")
            ?.takeIf { it in setOf("system", "light", "dark") } ?: "system"
        val main = Handler(Looper.getMainLooper())
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(boundService: XposedService) {
                main.post {
                    service = boundService
                    isModuleActive = true
                    frameworkDescription = "${boundService.frameworkName} ${boundService.frameworkVersion.substringBefore('\n').substringBefore(' ')}"
                    sync(app)
                    refreshSystemStatus(app)
                }
            }

            override fun onServiceDied(deadService: XposedService) {
                main.post {
                    if (service === deadService) {
                        service = null
                        isModuleActive = false
                        settingsSynced = false
                        systemHookCurrent = null
                        frameworkDescription = null
                    }
                }
            }
        })
    }

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isCameraSoundUnlockEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_CAMERA_SOUND_UNLOCK, DEFAULT_CAMERA_SOUND_UNLOCK)

    fun isEssentialVoiceUnlockEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_ESSENTIAL_VOICE_UNLOCK, DEFAULT_ESSENTIAL_VOICE_UNLOCK)

    fun setCameraSoundUnlockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CAMERA_SOUND_UNLOCK, enabled).apply()
        sync(context)
    }

    fun setEssentialVoiceUnlockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ESSENTIAL_VOICE_UNLOCK, enabled).apply()
        sync(context)
    }

    fun setHighContrastEnabled(context: Context, enabled: Boolean) {
        highContrastEnabled = enabled
        getPrefs(context).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun setThemeMode(context: Context, mode: String) {
        require(mode in setOf("system", "light", "dark"))
        themeMode = mode
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun sync(context: Context) {
        settingsSynced = try {
            service?.getRemotePreferences(PREFS_NAME)?.edit()
                ?.putBoolean(KEY_CAMERA_SOUND_UNLOCK, isCameraSoundUnlockEnabled(context))
                ?.putBoolean(KEY_ESSENTIAL_VOICE_UNLOCK, isEssentialVoiceUnlockEnabled(context))
                ?.commit() == true
        } catch (error: RuntimeException) {
            Log.e("Notrash_Config", "Could not sync settings", error)
            false
        }
    }

    fun refreshSystemStatus(context: Context) {
        systemHookCurrent = try {
            val bound = service
            if (bound == null || bound.apiVersion < 102) null else {
                val version = context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
                val target = bound.runningTargets.firstOrNull { it.processName in setOf("system", "system_server") }
                Log.i("Notrash_Config", "System hook: version=${target?.loadedVersionCode}, state=${target?.state}")
                target != null && target.loadedVersionCode == version &&
                    target.state == io.github.libxposed.service.HookedTarget.State.UP_TO_DATE
            }
        } catch (error: RuntimeException) {
            Log.w("Notrash_Config", "Cannot inspect system hook", error)
            null
        }
    }

    fun reloadSystemHook(context: Context, onResult: (Boolean) -> Unit) {
        try {
            val bound = service ?: return onResult(false)
            if (bound.apiVersion < 102) return onResult(false)
            val target = bound.runningTargets.firstOrNull { it.processName in setOf("system", "system_server") }
                ?: return onResult(false)
            bound.hotReloadModule(target, null) { _, result ->
                Log.i("Notrash_Config", "System hot reload: ${result.status}, ${result.message}")
                Handler(Looper.getMainLooper()).post {
                    refreshSystemStatus(context)
                    onResult(result.status == io.github.libxposed.service.HotReloadResult.Status.SUCCEEDED)
                }
            }
        } catch (error: RuntimeException) {
            Log.w("Notrash_Config", "System hot reload unavailable", error)
            onResult(false)
        }
    }
}
