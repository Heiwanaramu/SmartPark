package com.heiwanaramu.smartpark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.heiwanaramu.smartpark.MainActivity;
import com.heiwanaramu.smartpark.R;
import com.heiwanaramu.smartpark.api.ApiClient;
import com.heiwanaramu.smartpark.api.ApiService;
import com.heiwanaramu.smartpark.models.LoginResponse;
import com.heiwanaramu.smartpark.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoToRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.getAuthToken() != null) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Temporary bypass for testing
        if (email.equals("123") && password.equals("123")) {
            sessionManager.saveAuthToken("test-token-123");
            sessionManager.saveUserId("test-user-id");
            sessionManager.saveUserName("Test User");
            Toast.makeText(this, "Test Login Successful", Toast.LENGTH_SHORT).show();
            goToMainActivity();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(email, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    sessionManager.saveAuthToken(loginResponse.getToken());
                    sessionManager.saveUserId(loginResponse.getUser().getId());
                    sessionManager.saveUserName(loginResponse.getUser().getName());

                    Toast.makeText(LoginActivity.this, "Welcome " + loginResponse.getUser().getName(), Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Check credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
