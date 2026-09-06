package com.notrash.xposed.hooks;

import io.github.libxposed.api.XposedInterface;

public final class SystemUIHooks {
    public static void hook(XposedInterface module, ClassLoader loader) {
        EssentialVoiceHooks.hookNtFeaturesUtils(module, loader);
    }
}
