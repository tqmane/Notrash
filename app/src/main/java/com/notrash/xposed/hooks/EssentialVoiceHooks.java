package com.notrash.xposed.hooks;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.reflect.Method;
import com.notrash.xposed.ConfigReader;
import io.github.libxposed.api.XposedInterface;

public final class EssentialVoiceHooks {
    private static final String TAG = "Notrash_EssentialVoice";
    private static final String FEATURE = "NTF_ESSENTIAL_VOICE";

    public static void hookRecorder(XposedInterface module, ClassLoader loader) {
        try {
            Method check = loader.loadClass("f5.i").getDeclaredMethod("P", String.class);
            module.hook(check).intercept(chain -> {
                if (FEATURE.equals(chain.getArg(0)) && ConfigReader.isEssentialVoiceUnlockEnabled(null)) {
                    return true;
                }
                return chain.proceed();
            });
            Method onCreate = loader.loadClass("com.nothing.ntessentialrecorder.MainApplication")
                    .getDeclaredMethod("onCreate");
            module.hook(onCreate).intercept(chain -> {
                Object result = chain.proceed();
                Application app = (Application) chain.getThisObject();
                // The original app makes the same three component updates in z6.f.
                // Repeat on preference changes so turning the module off restores native support.
                ConfigReader.observeVoiceChanges(() -> new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        boolean supported = (Boolean) check.invoke(null, FEATURE);
                        int state = supported ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                        for (String name : new String[]{"EssentialVoiceIntroActivity",
                                "EssentialVoiceSettingsActivity", "EssentialVoiceTutorialActivity"}) {
                            app.getPackageManager().setComponentEnabledSetting(new ComponentName(app,
                                    "com.nothing.ntessentialrecorder." + name), state, PackageManager.DONT_KILL_APP);
                        }
                        Log.i(TAG, "Voice components updated: supported=" + supported);
                    } catch (Exception error) {
                        Log.e(TAG, "Cannot refresh Voice components", error);
                    }
                }));
                return result;
            });
            Log.i(TAG, "Recorder 16.0.60 feature gate hooked");
        } catch (ReflectiveOperationException | RuntimeException error) {
            Log.e(TAG, "Recorder version does not expose the supported Voice gate", error);
        }
        hookNtFeaturesUtils(module, loader);
    }

    public static void hookSpaceOrAiService(XposedInterface module, ClassLoader loader) {
        // Voice is implemented by Recorder. Do not spoof unrelated model/ASR engine flags.
        hookNtFeaturesUtils(module, loader);
    }

    public static void hookNtFeaturesUtils(XposedInterface module, ClassLoader loader) {
        try {
            Class<?> type = loader.loadClass("com.nothing.NtFeaturesUtils");
            int featureId = type.getField(FEATURE).getInt(null);
            for (Method method : type.getDeclaredMethods()) {
                if (!method.getName().equals("isSupport") || method.getReturnType() != boolean.class
                        || method.getParameterCount() != 1) continue;
                module.hook(method).intercept(chain -> {
                    if (ConfigReader.isEssentialVoiceUnlockEnabled(null)) {
                        Object arg = chain.getArg(0);
                        if (Integer.valueOf(featureId).equals(arg) || FEATURE.equals(arg)
                                || (arg instanceof int[] && ((int[]) arg).length == 1
                                && ((int[]) arg)[0] == featureId)) return true;
                    }
                    return chain.proceed();
                });
            }
            Log.i(TAG, "Nothing Voice feature hooked: " + featureId);
        } catch (ReflectiveOperationException | RuntimeException error) {
            Log.w(TAG, "Nothing framework Voice flag unavailable: " + error);
        }
    }
}
