package com.heiwanaramu.smartpark.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.heiwanaramu.smartpark.R;
import com.heiwanaramu.smartpark.utils.SessionManager;

public class SettingsActivity extends SmartParkActivity {

    private SessionManager sessionManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);
        prefs = getSharedPreferences("SmartParkSettings", MODE_PRIVATE);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        MaterialSwitch switchDarkMode = findViewById(R.id.switchDarkMode);
        TextView tvTextSizeValue = findViewById(R.id.tvTextSizeValue);

        // Load and Display User Info
        tvUserName.setText(sessionManager.getUserName());
        tvUserEmail.setText(sessionManager.getUserEmail());

        // Navigation to Edit Profile
        findViewById(R.id.layoutEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // Dark Mode Logic (Real Toggle)
        boolean isDark = prefs.getBoolean("dark_mode", true);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            recreate(); // Reload activity to apply theme
        });

        // Text Size Logic (Real Toggle)
        String currentSize = prefs.getString("text_size", "Medium");
        tvTextSizeValue.setText(currentSize);
        findViewById(R.id.layoutTextSize).setOnClickListener(v -> {
            String nextSize;
            switch (tvTextSizeValue.getText().toString()) {
                case "Small": nextSize = "Medium"; break;
                case "Medium": nextSize = "Large"; break;
                default: nextSize = "Small"; break;
            }
            tvTextSizeValue.setText(nextSize);
            prefs.edit().putString("text_size", nextSize).apply();
            recreate(); // Reload activity to apply text scale
        });

        // Contributors Navigation
        findViewById(R.id.layoutContributors).setOnClickListener(v -> {
            startActivity(new Intent(this, ContributorsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserName.setText(sessionManager.getUserName());
        tvUserEmail.setText(sessionManager.getUserEmail());
    }
}
