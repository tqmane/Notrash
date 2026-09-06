package com.notrash.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notrash.R
import com.notrash.data.NotrashConfig
import com.notrash.ui.components.SettingsActionEntry
import com.notrash.ui.components.SettingsNothingGroup
import com.notrash.ui.components.SettingsSectionHeader
import com.notrash.ui.components.SettingsToggleEntry
import com.notrash.ui.theme.NothingDotFontFamily
import com.notrash.ui.theme.NTypeFontFamily
import com.notrash.ui.theme.NothingRed
import com.notrash.ui.theme.NotrashTheme
import com.notrash.ui.theme.StatusGreen
import com.notrash.util.ShellUtils
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotrashConfig.initialize(this)
        enableEdgeToEdge()
        setContent {
            val darkTheme = when (NotrashConfig.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            NotrashTheme(
                highContrast = NotrashConfig.highContrastEnabled,
                darkTheme = darkTheme
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    var currentScreen by remember { mutableStateOf("home") }
                    val isModuleActive = NotrashConfig.isModuleActive

                    if (currentScreen == "about") {
                        BackHandler {
                            currentScreen = "home"
                        }
                        AboutScreen(
                            onBack = { currentScreen = "home" },
                            isModuleActive = isModuleActive
                        )
                    } else {
                        NotrashHomeScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            onNavigateToAbout = { currentScreen = "about" },
                            isModuleActive = isModuleActive
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NotrashHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToAbout: () -> Unit,
    isModuleActive: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var cameraSoundUnlock by remember {
        mutableStateOf(NotrashConfig.isCameraSoundUnlockEnabled(context))
    }
    var essentialVoiceUnlock by remember {
        mutableStateOf(NotrashConfig.isEssentialVoiceUnlockEnabled(context))
    }

    var applying by remember { mutableStateOf(false) }
    val applySettings = {
        if (!applying) {
            applying = true
            coroutineScope.launch {
                NotrashConfig.sync(context)
                val result = if (NotrashConfig.settingsSynced) {
                    ShellUtils.restartTargetApps()
                } else {
                    ShellUtils.Result.Error(context.getString(R.string.settings_sync_failed))
                }
                val message = when (result) {
                    is ShellUtils.Result.Success -> context.getString(R.string.apply_success)
                    is ShellUtils.Result.PermissionDenied -> context.getString(R.string.apply_no_root)
                    is ShellUtils.Result.Error -> context.getString(R.string.apply_failed, result.message)
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                applying = false
            }
        }
    }

    Column(
        modifier = modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 600.dp)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp)

    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NOTRASH",
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = NTypeFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NothingRed)
                )
            }
            IconButton(onClick = onNavigateToAbout) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.about_screen_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Module Status Card
        SettingsNothingGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isModuleActive) MaterialTheme.colorScheme.primary else NothingRed)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isModuleActive) {
                            stringResource(R.string.status_active)
                        } else {
                            stringResource(R.string.status_inactive)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isModuleActive) {
                            stringResource(if (NotrashConfig.settingsSynced) R.string.status_active_desc else R.string.settings_sync_failed)
                        } else {
                            stringResource(R.string.status_inactive_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Camera Section
        SettingsSectionHeader(title = stringResource(R.string.category_camera))
        SettingsNothingGroup {
            SettingsToggleEntry(
                title = stringResource(R.string.pref_camera_sound_unlock_title),
                description = stringResource(R.string.pref_camera_sound_unlock_desc),
                checked = cameraSoundUnlock,
                icon = Icons.Outlined.CameraAlt,
                onCheckedChange = { isChecked ->
                    cameraSoundUnlock = isChecked
                    NotrashConfig.setCameraSoundUnlockEnabled(context, isChecked)

                }
            )
        }

        // Essential Features Section
        SettingsSectionHeader(title = stringResource(R.string.category_essential))
        SettingsNothingGroup {
            SettingsToggleEntry(
                title = stringResource(R.string.pref_essential_voice_unlock_title),
                description = stringResource(R.string.pref_essential_voice_unlock_desc),
                checked = essentialVoiceUnlock,
                icon = Icons.Outlined.GraphicEq,
                onCheckedChange = { isChecked ->
                    essentialVoiceUnlock = isChecked
                    NotrashConfig.setEssentialVoiceUnlockEnabled(context, isChecked)

                }
            )
        }

        if (essentialVoiceUnlock) {
            if (NotrashConfig.systemHookCurrent == false) {
                Text(
                    text = stringResource(R.string.system_hook_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                )
                SettingsNothingGroup {
                    SettingsActionEntry(
                        title = stringResource(R.string.reload_system_hook),
                        description = stringResource(R.string.reload_system_hook_desc),
                        onClick = {
                            NotrashConfig.reloadSystemHook(context) { success ->
                                Toast.makeText(context,
                                    if (success) R.string.reload_success else R.string.reload_unavailable,
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
            SettingsNothingGroup(modifier = Modifier.padding(top = 4.dp)) {
                SettingsActionEntry(
                    title = stringResource(R.string.open_voice),
                    description = stringResource(R.string.open_voice_desc),
                    onClick = {
                        try {
                            context.startActivity(android.content.Intent().setClassName(
                                "com.nothing.ntessentialrecorder",
                                "com.nothing.ntessentialrecorder.EssentialVoiceIntroActivity"
                            ))
                        } catch (error: RuntimeException) {
                            Toast.makeText(context, R.string.voice_unavailable, Toast.LENGTH_LONG).show()
                            try {
                                context.startActivity(android.content.Intent().setClassName(
                                    "com.nothing.ntessentialrecorder",
                                    "com.nothing.ntessentialrecorder.SettingsActivity"
                                ))
                            } catch (missing: RuntimeException) {
                                android.util.Log.w("Notrash_UI", "Recorder unavailable", missing)
                            }
                        }
                    }
                )
            }
        }

        SettingsSectionHeader(title = stringResource(R.string.category_appearance))
        SettingsNothingGroup {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val modes = listOf("system" to R.string.theme_system, "light" to R.string.theme_light,
                        "dark" to R.string.theme_dark)
                    modes.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = NotrashConfig.themeMode == mode,
                            onClick = { NotrashConfig.setThemeMode(context, mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                activeBorderColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text(stringResource(label)) }
                    }
                }
            }
        }
        SettingsNothingGroup(modifier = Modifier.padding(top = 4.dp)) {
            SettingsToggleEntry(
                title = stringResource(R.string.high_contrast_title),
                description = stringResource(R.string.high_contrast_desc),
                checked = NotrashConfig.highContrastEnabled,
                onCheckedChange = { NotrashConfig.setHighContrastEnabled(context, it) },
                icon = Icons.Outlined.Contrast
            )
        }

        SettingsSectionHeader(title = stringResource(R.string.category_system))
        SettingsNothingGroup {
            SettingsActionEntry(
                title = stringResource(if (applying) R.string.applying else R.string.apply_settings),
                description = stringResource(R.string.apply_settings_desc),
                onClick = { applySettings() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
