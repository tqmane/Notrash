package com.notrash.xposed.hooks;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import com.notrash.xposed.ConfigReader;

public final class CameraHooks {

    private static final String TAG = "Notrash_Camera";
    private static final String PREF_SHUTTER_SOUND_KEY = "pref_shutter_sound_key";

    public static void hook(XposedInterface module, ClassLoader classLoader) {
        // Try both common package patterns
        Class<?> productConfigClass = findClass(classLoader,
                "com.nothing.common.utils.ProductConfig",
                "com.nothing.camera.util.ProductConfig");

        if (productConfigClass == null) {
            // Not camera DEX, skip
            return;
        }

        Log.i(TAG, "Initializing CameraHooks for com.nothing.camera (found ProductConfig in " + productConfigClass.getName() + ")");

        // 1. Hook ProductConfig
        try {
            hookCameraConditional(module, productConfigClass, "isCameraSoundForced", false);
            hookCameraConditional(module, productConfigClass, "isSupportShutterSound", true);
            Log.i(TAG, "ProductConfig hooked successfully");
        } catch (Throwable t) {
            Log.w(TAG, "Failed to hook ProductConfig: " + t.getMessage());
        }

        // 2. Hook Utils in camera
        Class<?> utilsClass = findClass(classLoader,
                "com.nothing.common.setting.Utils",
                "com.nothing.camera.util.Utils");
        if (utilsClass != null) {
            try {
                Method initialize = utilsClass.getDeclaredMethod("initialize", Context.class, int.class, int.class);
                module.hook(initialize).intercept(chain -> {
                    Object result = chain.proceed();
                    if (ConfigReader.isCameraSoundUnlockEnabled((Context) chain.getArg(0))) {
                        setStaticBooleanFieldIfExists(productConfigClass, "isCameraSoundForced", false);
                        setStaticBooleanFieldIfExists(productConfigClass, "isSupportShutterSound", true);
                    }
                    return result;
                });
                hookCameraConditional(module, utilsClass, "isCameraSoundForced", false);
                hookCameraConditional(module, utilsClass, "isConfigCameraSoundForced", false);
                Log.i(TAG, "Utils hooked successfully");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook Utils: " + t.getMessage());
            }
        }

        // 3. Hook Util.isInSilentMode -> return false if enabled
        Class<?> utilClass = findClass(classLoader,
                "com.nothing.common.utils.Util",
                "com.nothing.camera.util.Util");
        if (utilClass != null) {
            try {
                for (Method m : utilClass.getDeclaredMethods()) {
                    if ("isInSilentMode".equals(m.getName()) && m.getReturnType() == boolean.class) {
                        module.hook(m).intercept(chain -> {
                            Context ctx = chain.getArgs().size() > 0 && chain.getArg(0) instanceof Context
                                    ? (Context) chain.getArg(0) : null;
                            if (ConfigReader.isCameraSoundUnlockEnabled(ctx)) {
                                return false;
                            }
                            return chain.proceed();
                        });
                        Log.i(TAG, "Util.isInSilentMode hooked successfully");
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook Util.isInSilentMode: " + t.getMessage());
            }
        }

        // 4. Hook ListPreference to prevent disabling pref_shutter_sound_key
        Class<?> listPrefClass = findClass(classLoader,
                "com.nothing.common.preference.ListPreference",
                "com.nothing.camera.preference.ListPreference");
        if (listPrefClass != null) {
            try {
                for (Method m : listPrefClass.getDeclaredMethods()) {
                    if ("setOverrideValue".equals(m.getName()) && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == String.class) {
                        module.hook(m).intercept(chain -> {
                            if (!ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                return chain.proceed();
                            }
                            Object thiz = chain.getThisObject();
                            String key = getPrefKey(thiz);
                            if (PREF_SHUTTER_SOUND_KEY.equals(key)) {
                                Object arg = chain.getArg(0);
                                if ("off".equals(arg)) {
                                    return chain.proceed(new Object[]{null});
                                }
                            }
                            return chain.proceed();
                        });
                    }
                    if ("setClickable".equals(m.getName()) && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == boolean.class) {
                        module.hook(m).intercept(chain -> {
                            if (!ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                return chain.proceed();
                            }
                            Object thiz = chain.getThisObject();
                            String key = getPrefKey(thiz);
                            if (PREF_SHUTTER_SOUND_KEY.equals(key)) {
                                return chain.proceed(new Object[]{true});
                            }
                            return chain.proceed();
                        });
                    }
                    if ("setEnabled".equals(m.getName()) && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == boolean.class) {
                        module.hook(m).intercept(chain -> {
                            if (!ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                return chain.proceed();
                            }
                            Object thiz = chain.getThisObject();
                            String key = getPrefKey(thiz);
                            if (PREF_SHUTTER_SOUND_KEY.equals(key)) {
                                return chain.proceed(new Object[]{true});
                            }
                            return chain.proceed();
                        });
                    }
                }
                Log.i(TAG, "ListPreference hooked successfully");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook ListPreference: " + t.getMessage());
            }
        }

        // 5. Hook SettingGroupsManager
        Class<?> sgmClass = findClass(classLoader,
                "com.nothing.common.setting.SettingGroupsManager",
                "com.nothing.camera.setting.SettingGroupsManager");
        if (sgmClass != null) {
            try {
                for (Method m : sgmClass.getDeclaredMethods()) {
                    if (m.getName().equals("filterShutterSoundPreference")) {
                        module.hook(m).intercept(chain -> {
                            Object res = chain.proceed();
                            if (ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                try {
                                    for (Object arg : chain.getArgs()) {
                                        if (arg != null && PREF_SHUTTER_SOUND_KEY.equals(getPrefKey(arg))) {
                                            unblockPref(arg);
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                            return res;
                        });
                    }
                }
                Log.i(TAG, "SettingGroupsManager hooked successfully");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook SettingGroupsManager: " + t.getMessage());
            }
        }

        // 6. Hook SettingApplier
        Class<?> saClass = findClass(classLoader,
                "com.nothing.common.setting.SettingApplier",
                "com.nothing.camera.setting.SettingApplier");
        if (saClass != null) {
            try {
                for (Method m : saClass.getDeclaredMethods()) {
                    if (m.getName().equals("applyCaptureRequestBuilderToUI")) {
                        module.hook(m).intercept(chain -> {
                            Object res = chain.proceed();
                            if (ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                try {
                                    Object thiz = chain.getThisObject();
                                    if (thiz != null) {
                                        Field prefField = findFieldByTypeName(thiz.getClass(), "ListPreference");
                                        if (prefField != null) {
                                            prefField.setAccessible(true);
                                            Object pref = prefField.get(thiz);
                                            if (pref != null && PREF_SHUTTER_SOUND_KEY.equals(getPrefKey(pref))) {
                                                unblockPref(pref);
                                            }
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                            return res;
                        });
                    }
                }
                Log.i(TAG, "SettingApplier hooked successfully");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook SettingApplier: " + t.getMessage());
            }
        }

        // 7. Hook SettingContext.isShutterSoundEnabled()
        Class<?> scClass = findClass(classLoader,
                "com.nothing.common.setting.SettingContext",
                "com.nothing.camera.setting.SettingContext");
        if (scClass != null) {
            try {
                for (Method m : scClass.getDeclaredMethods()) {
                    if (m.getName().equals("isShutterSoundEnabled") && m.getParameterCount() == 0
                            && m.getReturnType() == boolean.class) {
                        module.hook(m).intercept(chain -> {
                            if (!ConfigReader.isCameraSoundUnlockEnabled(null)) {
                                return chain.proceed();
                            }
                            try {
                                Object thiz = chain.getThisObject();
                                if (thiz != null) {
                                    Method getStringMethod = thiz.getClass().getMethod("getString", String.class, String.class);
                                    Object val = getStringMethod.invoke(thiz, PREF_SHUTTER_SOUND_KEY, "on");
                                    return !"off".equals(val);
                                }
                            } catch (Throwable ignored) {
                            }
                            return chain.proceed();
                        });
                    }
                }
                Log.i(TAG, "SettingContext hooked successfully");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to hook SettingContext: " + t.getMessage());
            }
        }
    }

    private static Class<?> findClass(ClassLoader loader, String... names) {
        for (String name : names) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static void setStaticBooleanFieldIfExists(Class<?> clazz, String fieldName, boolean value) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.setBoolean(null, value);
        } catch (Throwable ignored) {
        }
    }

    private static void hookCameraConditional(XposedInterface module, Class<?> clazz, String methodName, final boolean unlockedReturnValue) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && m.getReturnType() == boolean.class) {
                try {
                    module.hook(m).intercept(chain -> {
                        if (ConfigReader.isCameraSoundUnlockEnabled(null)) {
                            return unlockedReturnValue;
                        }
                        return chain.proceed();
                    });
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to hook " + clazz.getSimpleName() + "." + methodName + ": " + t.getMessage());
                }
            }
        }
    }

    private static String getPrefKey(Object prefInstance) {
        if (prefInstance == null) return null;
        try {
            Method getKeyMethod = prefInstance.getClass().getMethod("getKey");
            Object key = getKeyMethod.invoke(prefInstance);
            if (key instanceof String) return (String) key;
        } catch (Throwable ignored) {
        }
        try {
            Field mKeyField = prefInstance.getClass().getField("mKey");
            mKeyField.setAccessible(true);
            Object key = mKeyField.get(prefInstance);
            if (key instanceof String) return (String) key;
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void unblockPref(Object pref) {
        try {
            Method setOverride = pref.getClass().getMethod("setOverrideValue", String.class);
            setOverride.invoke(pref, (String) null);
        } catch (Throwable ignored) {
        }
        try {
            Method setClickable = pref.getClass().getMethod("setClickable", boolean.class);
            setClickable.invoke(pref, true);
        } catch (Throwable ignored) {
        }
        try {
            Method setEnabled = pref.getClass().getMethod("setEnabled", boolean.class);
            setEnabled.invoke(pref, true);
        } catch (Throwable ignored) {
        }
    }

    private static Field findFieldByTypeName(Class<?> clazz, String typeSimpleName) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType().getSimpleName().equals(typeSimpleName)) {
                return f;
            }
        }
        return null;
    }
}
