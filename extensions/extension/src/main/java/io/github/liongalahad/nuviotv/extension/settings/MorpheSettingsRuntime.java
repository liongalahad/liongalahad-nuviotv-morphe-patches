package io.github.liongalahad.nuviotv.extension.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;

/** Process-local bridge used by injected bytecode and Nuvio's native settings pane. */
@SuppressWarnings({"unused", "JavaReflectionMemberAccess"})
public final class MorpheSettingsRuntime {
    public static final String PREFERENCES_NAME = "morphe_patches";
    public static final String REMOVE_SDH_KEY = "subtitles.remove_sdh_annotations";

    private static volatile Application application;
    private static volatile SharedPreferences preferences;
    private static volatile boolean removeSdhEnabled;
    private static volatile boolean subtitlesExpanded;

    private MorpheSettingsRuntime() {}

    /** Reuses Nuvio's hidden EXPERIENCE slot only inside its visibility filter. */
    public static int mapVisibilityOrdinal(int ordinal) {
        return ordinal == 0 ? 4 : ordinal;
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
        persistRemoveSdhEnabled(enabled);
    }

    /** Toggles the preference synchronously and returns the new value. */
    public static boolean toggleRemoveSdhEnabled() {
        if (preferences == null) {
            Application current = currentApplication();
            if (current != null) initialize(current);
        }
        if (preferences == null) {
            throw new IllegalStateException("Morphe settings were not initialized");
        }
        boolean enabled = !removeSdhEnabled;
        persistRemoveSdhEnabled(enabled);
        return enabled;
    }

    public static boolean isSubtitlesExpanded() {
        return subtitlesExpanded;
    }

    public static boolean toggleSubtitlesExpanded() {
        subtitlesExpanded = !subtitlesExpanded;
        return subtitlesExpanded;
    }

    public static String subtitlesExpansionStatus() {
        return subtitlesExpanded ? "Open" : "Closed";
    }

    private static void persistRemoveSdhEnabled(boolean enabled) {
        removeSdhEnabled = enabled;
        // The click must survive an immediate force-stop or device restart.
        preferences.edit().putBoolean(REMOVE_SDH_KEY, enabled).commit();
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
