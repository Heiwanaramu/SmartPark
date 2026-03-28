package com.heiwanaramu.smartpark.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

public abstract class SmartParkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences prefs = getSharedPreferences("SmartParkSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", true);
        
        // Use setLocalNightMode for activity-level control or setDefaultNightMode for global
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("SmartParkSettings", MODE_PRIVATE);
        String textSize = prefs.getString("text_size", "Medium");
        float scale = 1.0f;
        switch (textSize) {
            case "Small": scale = 0.85f; break;
            case "Large": scale = 1.25f; break;
            default: scale = 1.0f; break;
        }

        Configuration overrideConfiguration = newBase.getResources().getConfiguration();
        overrideConfiguration.fontScale = scale;
        Context context = newBase.createConfigurationContext(overrideConfiguration);
        super.attachBaseContext(context);
    }
}
