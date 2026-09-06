package com.notrash.xposed;

import android.util.Log;

import androidx.annotation.NonNull;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

import com.notrash.xposed.hooks.CameraHooks;
import com.notrash.xposed.hooks.EssentialVoiceHooks;
import com.notrash.xposed.hooks.SystemUIHooks;
import com.notrash.xposed.hooks.SystemServerHooks;

public class NotrashModule extends XposedModule {

    private static final String TAG = "Notrash_Module";
    private ClassLoader systemServerLoader;

    public NotrashModule() {
        super();
        Log.i(TAG, "NotrashModule instantiated (API 102)");
    }

    @Override
    public void onModuleLoaded(@NonNull XposedModuleInterface.ModuleLoadedParam param) {
        super.onModuleLoaded(param);
        Log.i(TAG, "NotrashModule loaded into process: " + param.getProcessName());
        ConfigReader.initialize(this);
    }

    @Override
    public void onPackageReady(@NonNull XposedModuleInterface.PackageReadyParam param) {
        super.onPackageReady(param);
        if (!param.isFirstPackage()) return;
        String packageName = param.getPackageName();
        ClassLoader classLoader = param.getClassLoader();

        Log.i(TAG, "onPackageReady: pkg=" + packageName + ", classLoader=" + classLoader);

        // Options are read at invocation time; loading the module does not enable them.
        if ("com.nothing.camera".equals(packageName)) {
            CameraHooks.hook(this, classLoader);
        } else if ("com.nothing.ntessentialrecorder".equals(packageName)) {
            EssentialVoiceHooks.hookRecorder(this, classLoader);
        } else if ("com.nothing.ntessentialspace".equals(packageName) || "com.nothing.ai.service".equals(packageName)) {
            EssentialVoiceHooks.hookSpaceOrAiService(this, classLoader);
        } else if ("com.android.systemui".equals(packageName)) {
            SystemUIHooks.hook(this, classLoader);
        }
    }

    @Override
    public void onSystemServerStarting(@NonNull XposedModuleInterface.SystemServerStartingParam param) {
        super.onSystemServerStarting(param);
        Log.i(TAG, "onSystemServerStarting");
        systemServerLoader = param.getClassLoader();
        SystemServerHooks.hook(this, param.getClassLoader());
    }

    @Override
    public boolean onHotReloading(@NonNull XposedModuleInterface.HotReloadingParam param) {
        if (systemServerLoader == null) return false;
        ConfigReader.close();
        // Both values belong to the OS classloader, not this module generation.
        param.setSavedInstanceState(new Object[]{systemServerLoader, SystemServerHooks.retire()});
        return true;
    }

    @Override
    public void onHotReloaded(@NonNull XposedModuleInterface.HotReloadedParam param) {
        super.onHotReloaded(param);
        Object[] state = (Object[]) param.getSavedInstanceState();
        systemServerLoader = (ClassLoader) state[0];
        SystemServerHooks.restore(state[1]);
        ConfigReader.initialize(this);
        SystemServerHooks.hook(this, systemServerLoader);
    }

}
