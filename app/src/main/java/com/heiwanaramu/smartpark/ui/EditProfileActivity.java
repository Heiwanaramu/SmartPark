package com.heiwanaramu.smartpark.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.heiwanaramu.smartpark.R;
import com.heiwanaramu.smartpark.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputEditText etName, etEmail, etAddress, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        // Load current data
        etName.setText(sessionManager.getUserName());
        etEmail.setText(sessionManager.getUserEmail());
        etAddress.setText(sessionManager.getUserAddress());
        etPhone.setText(sessionManager.getUserPhone());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.saveUserName(name);
            sessionManager.saveUserEmail(email);
            sessionManager.saveUserAddress(address);
            sessionManager.saveUserPhone(phone);

            Toast.makeText(this, "Profile Updated Locally", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
