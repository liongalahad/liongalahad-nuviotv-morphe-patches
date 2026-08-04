package io.github.liongalahad.nuviotv.extension.settings;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/** Process-local bridge used by injected bytecode and the settings activity. */
@SuppressWarnings({"unused", "JavaReflectionMemberAccess"})
public final class MorpheSettingsRuntime {
    public static final String PREFERENCES_NAME = "morphe_patches";
    public static final String REMOVE_SDH_KEY = "subtitles.remove_sdh_annotations";

    private static final AtomicBoolean SETTINGS_OPEN = new AtomicBoolean(false);
    private static volatile Application application;
    private static volatile SharedPreferences preferences;
    private static volatile boolean removeSdhEnabled;

    private MorpheSettingsRuntime() {}

    /** Reuses Nuvio's hidden EXPERIENCE slot only inside its visibility filter. */
    public static int mapVisibilityOrdinal(int ordinal) {
        return ordinal == 0 ? 4 : ordinal;
    }

    /** Intercepts a click only for the hidden slot repurposed as Morphe. */
    public static boolean openIfMorphe(Object category) {
        if (!(category instanceof Enum) || !"EXPERIENCE".equals(((Enum<?>) category).name())) {
            return false;
        }
        Application application = currentApplication();
        if (application == null) return false;
        initialize(application);
        if (SETTINGS_OPEN.compareAndSet(false, true)) {
            Intent intent = new Intent(application, MorpheSettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            application.startActivity(intent);
        }
        return true;
    }

    public static void initialize(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof Application) application = (Application) appContext;
        if (preferences != null) return;
        synchronized (MorpheSettingsRuntime.class) {
            if (preferences != null) return;
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            removeSdhEnabled = prefs.getBoolean(REMOVE_SDH_KEY, false);
            prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                if (REMOVE_SDH_KEY.equals(key)) {
                    removeSdhEnabled = sharedPreferences.getBoolean(REMOVE_SDH_KEY, false);
                }
            });
            preferences = prefs;
        }
    }

    /** Called once for every outgoing cue batch; state changes are immediately visible. */
    public static boolean isRemoveSdhEnabled() {
        if (preferences == null) {
            Application application = currentApplication();
            if (application != null) initialize(application);
        }
        return removeSdhEnabled;
    }

    public static void setRemoveSdhEnabled(Context context, boolean enabled) {
        initialize(context);
        removeSdhEnabled = enabled;
        preferences.edit().putBoolean(REMOVE_SDH_KEY, enabled).apply();
    }

    static SharedPreferences preferences(Context context) {
        initialize(context);
        return preferences;
    }

    static void settingsClosed() {
        SETTINGS_OPEN.set(false);
    }

    private static Application currentApplication() {
        Application cached = application;
        if (cached != null) return cached;
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method method = activityThread.getDeclaredMethod("currentApplication");
            Application reflected = (Application) method.invoke(null);
            application = reflected;
            return reflected;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
