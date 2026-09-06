package com.notrash.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import io.github.libxposed.api.XposedInterface;

public final class ConfigReader {
    private static SharedPreferences preferences;
    private static SharedPreferences.OnSharedPreferenceChangeListener voiceListener;

    public static void close() {
        if (preferences != null && voiceListener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(voiceListener);
        }
        voiceListener = null;
        preferences = null;
    }

    public static void observeVoiceChanges(Runnable callback) {
        if (preferences == null) return;
        if (voiceListener != null) preferences.unregisterOnSharedPreferenceChangeListener(voiceListener);
        voiceListener = (prefs, key) -> {
            if (key == null || "essential_voice_unlock".equals(key)) callback.run();
        };
        preferences.registerOnSharedPreferenceChangeListener(voiceListener);
    }

    public static void initialize(XposedInterface module) {
        try {
            preferences = module.getRemotePreferences("notrash_config");
            Log.i("Notrash_Config", "Framework settings connected: camera="
                    + isCameraSoundUnlockEnabled(null) + ", voice="
                    + isEssentialVoiceUnlockEnabled(null));
        } catch (RuntimeException error) {
            preferences = null;
            Log.e("Notrash_Config", "Framework settings unavailable; features disabled", error);
        }
    }

    public static boolean isCameraSoundUnlockEnabled(Context context) {
        return getBoolean("camera_sound_unlock");
    }

    public static boolean isEssentialVoiceUnlockEnabled(Context context) {
        return getBoolean("essential_voice_unlock");
    }

    private static boolean getBoolean(String key) {
        try {
            return preferences != null && preferences.getBoolean(key, false);
        } catch (RuntimeException error) {
            Log.e("Notrash_Config", "Cannot read " + key, error);
            return false;
        }
    }
}
