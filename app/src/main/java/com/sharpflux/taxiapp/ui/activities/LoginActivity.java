package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private EditText etUsername, etPassword;
    private ImageView ivPasswordToggle;
    private TextView tvErrorMessage;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Initialize ViewModel correctly
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(AuthViewModel.class);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }

    private void setupClickListeners() {
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        LinearLayout btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        LinearLayout btnAppleLogin = findViewById(R.id.btnAppleLogin);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            tvErrorMessage.setVisibility(View.GONE);

            if (user.isEmpty()) {
                showError("Please enter username");
                return;
            }
            if (pass.isEmpty()) {
                showError("Please enter password");
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            viewModel.login(user, pass).observe(this, success -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (success != null && success) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    showError("Invalid username or password");
                }
            });
        });

        ivPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivPasswordToggle.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password coming soon", Toast.LENGTH_SHORT).show()
        );

        tvSignUp.setOnClickListener(v -> {
            // Navigate to SignUpActivity
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        btnGoogleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Google login coming soon", Toast.LENGTH_SHORT).show()
        );

        btnAppleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Apple login coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
}