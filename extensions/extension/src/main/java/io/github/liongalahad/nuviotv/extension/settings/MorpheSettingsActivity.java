package io.github.liongalahad.nuviotv.extension.settings;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Dependency-free, D-pad-friendly settings UI injected into NuvioTV. */
@SuppressWarnings("deprecation")
public final class MorpheSettingsActivity extends Activity {
    private static final String SUBTITLES_METADATA =
            "io.github.liongalahad.nuviotv.settings.category.subtitles";
    private boolean showingCategory;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        MorpheSettingsRuntime.initialize(this);
        getWindow().setStatusBarColor(Color.rgb(17, 24, 39));
        getWindow().setNavigationBarColor(Color.rgb(17, 24, 39));
        showHub();
    }

    private void showHub() {
        showingCategory = false;
        LinearLayout content = content("Morphe", "Configure installed patches");
        if (categoryRegistered(SUBTITLES_METADATA)) {
            Button subtitles = button("Subtitles");
            subtitles.setOnClickListener(ignored -> showSubtitles());
            content.addView(subtitles, rowParams());
            subtitles.requestFocus();
        }
        setContentView(scroll(content));
    }

    private void showSubtitles() {
        showingCategory = true;
        LinearLayout content = content("Subtitles", "Media3 / ExoPlayer");
        Switch removeSdh = new Switch(this);
        removeSdh.setText("Remove SDH Annotations");
        removeSdh.setTextColor(Color.WHITE);
        removeSdh.setTextSize(20);
        removeSdh.setGravity(Gravity.CENTER_VERTICAL);
        removeSdh.setPadding(dp(24), dp(16), dp(24), dp(16));
        removeSdh.setFocusable(true);
        removeSdh.setChecked(MorpheSettingsRuntime.preferences(this).getBoolean(
                MorpheSettingsRuntime.REMOVE_SDH_KEY, false));
        removeSdh.setOnCheckedChangeListener((CompoundButton ignored, boolean checked) ->
                MorpheSettingsRuntime.setRemoveSdhEnabled(this, checked));
        content.addView(removeSdh, rowParams());
        removeSdh.requestFocus();
        setContentView(scroll(content));
    }

    @Override
    public void onBackPressed() {
        if (showingCategory) showHub(); else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        MorpheSettingsRuntime.settingsClosed();
        super.onDestroy();
    }

    private boolean categoryRegistered(String key) {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData != null && info.metaData.getBoolean(key, false);
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private LinearLayout content(String title, String subtitle) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(56), dp(40), dp(56), dp(40));
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(30);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        layout.addView(heading, rowParams());

        TextView description = new TextView(this);
        description.setText(subtitle);
        description.setTextColor(Color.LTGRAY);
        description.setTextSize(16);
        description.setPadding(0, dp(4), 0, dp(24));
        layout.addView(description, rowParams());
        return layout;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(20);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setFocusable(true);
        return button;
    }

    private ScrollView scroll(View child) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(17, 24, 39));
        scroll.addView(child);
        return scroll;
    }

    private LinearLayout.LayoutParams rowParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
