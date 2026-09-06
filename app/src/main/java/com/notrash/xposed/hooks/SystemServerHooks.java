package com.notrash.xposed.hooks;

import android.util.Log;
import com.notrash.xposed.ConfigReader;
import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Nothing OS 16: the extension factory caches a no-op Voice service on unsupported devices. */
public final class SystemServerHooks {
    private static final String TAG = "Notrash_SystemVoice";
    private static Object voiceService;
    private static boolean creating;
    private static boolean retired;

    public static synchronized Object retire() {
        retired = true;
        Object service = voiceService;
        voiceService = null;
        return service;
    }

    public static synchronized void restore(Object service) {
        voiceService = service;
        retired = false;
    }

    public static void hook(XposedInterface module, ClassLoader loader) {
        try {
            Class<?> factory = loader.loadClass("com.nothing.server.NtExtServiceFactory");
            Class<?> type = loader.loadClass("com.nothing.server.INtExtServiceFactory$ExtType");
            Method getOrCreate = factory.getDeclaredMethod("getOrCreate", type);
            module.hook(getOrCreate).intercept(chain -> {
                Object nativeService = chain.proceed();
                if (!"NT_ESSENTIAL_VOICE".equals(((Enum<?>) chain.getArg(0)).name())
                        || !ConfigReader.isEssentialVoiceUnlockEnabled(null)) return nativeService;
                if (nativeService.getClass().getName().equals("com.android.server.wm.NtEssentialVoiceImpl")) {
                    return nativeService;
                }
                try {
                    Object service = getVoiceService(factory);
                    return service != null ? service : nativeService;
                } catch (ReflectiveOperationException | RuntimeException error) {
                    Log.e(TAG, "Cannot create native Voice service", error);
                    return nativeService;
                }
            });
            ConfigReader.observeVoiceChanges(() -> {
                synchronized (SystemServerHooks.class) {
                    if (voiceService == null) return;
                    try {
                        if (!ConfigReader.isEssentialVoiceUnlockEnabled(null)) {
                            voiceService.getClass().getMethod("stopVoiceInput", boolean.class)
                                    .invoke(voiceService, false);
                        }
                        // Refresh IME flags through the native observer, including when switching off.
                        voiceService.getClass().getMethod("onUserSwitching").invoke(voiceService);
                    } catch (ReflectiveOperationException | RuntimeException error) {
                        Log.e(TAG, "Cannot refresh native Voice state", error);
                    }
                }
            });
            Log.i(TAG, "Dynamic native Voice factory hooked");
        } catch (ReflectiveOperationException | RuntimeException error) {
            Log.w(TAG, "Nothing OS native Voice factory unavailable", error);
        }
    }

    private static synchronized Object getVoiceService(Class<?> factory) throws ReflectiveOperationException {
        if (voiceService != null) return voiceService;
        if (retired) return null;
        if (creating) return null; // Native construction can synchronously ask for IME flags.
        Object instance = factory.getMethod("getInstance").invoke(null);
        // Find by type, not the obfuscated injector field name, which changes between OS builds.
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.getType().getName().equals("com.nothing.server.NtServiceInjector")) continue;
            field.setAccessible(true);
            Class<?> implementation = instance.getClass().getClassLoader()
                    .loadClass("com.android.server.wm.NtEssentialVoiceImpl");
            creating = true;
            try {
                voiceService = implementation.getConstructor(field.getType()).newInstance(field.get(instance));
            } finally {
                creating = false;
            }
            implementation.getMethod("onUserSwitching").invoke(voiceService);
            Log.i(TAG, "Native Voice service created");
            return voiceService;
        }
        throw new NoSuchFieldException("NtServiceInjector");
    }
}
