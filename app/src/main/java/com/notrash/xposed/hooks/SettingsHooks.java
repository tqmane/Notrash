package com.notrash.xposed.hooks;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import com.notrash.xposed.ConfigReader;

public final class SettingsHooks {
    private static final String TAG = "Notrash_Settings";

    public static void hook(XposedInterface module, ClassLoader classLoader) {
        try {
            Class<?> features = classLoader.loadClass("com.nothing.NtFeatures");
            Class<?> utils = classLoader.loadClass("com.nothing.NtFeaturesUtils");
            Field allDayAod = features.getField("NTF_ALL_DAY_AOD");
            int featureId = allDayAod.getInt(null);
            Method isSupport = utils.getDeclaredMethod("isSupport", int[].class);
            module.hook(isSupport).intercept(chain -> {
                int[] requested = (int[]) chain.getArg(0);
                if (ConfigReader.isAlwaysOnDisplayUnlockEnabled()) {
                    for (int feature : requested) {
                        if (feature == featureId) return true;
                    }
                }
                return chain.proceed();
            });
            Log.i(TAG, "NTF_ALL_DAY_AOD hooked successfully");
        } catch (Throwable error) {
            Log.e(TAG, "Failed to hook NTF_ALL_DAY_AOD", error);
        }

        try {
            Class<?> batteryInformationPolicy = classLoader.loadClass("tj.c");
            Method isAvailable = batteryInformationPolicy.getDeclaredMethod("a");
            module.hook(isAvailable).intercept(chain -> {
                if (ConfigReader.isBatteryInformationUnlockEnabled()) return true;
                return chain.proceed();
            });
            Log.i(TAG, "Battery information availability hooked successfully");
        } catch (Throwable error) {
            Log.e(TAG, "Failed to hook battery information availability", error);
        }
    }
}
