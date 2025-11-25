package com.sharpflux.taxiapp;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class TaxiApplication extends Application {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "app_theme";

    @Override
    public void onCreate() {
        super.onCreate();

        // Load and apply saved theme
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_THEME, false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}