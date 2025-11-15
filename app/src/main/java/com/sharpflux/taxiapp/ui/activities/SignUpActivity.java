package com.sharpflux.taxiapp.ui.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.Registration;
import com.sharpflux.taxiapp.data.repository.RegistrationRepository;

import org.json.JSONObject;
import android.content.Intent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
public class SignUpActivity extends AppCompatActivity {

    // Step 1 - Personal Information (Only fields shown to user)
    private EditText etFirstName, etMiddleName, etLastName, etEmail, etPassword, etConfirmPassword;

    // UI Components
    private ImageButton btnBackArrow, btnTogglePassword, btnToggleConfirmPassword;
    private Button btnSignUp;
    private LinearLayout btnGoogle, btnApple;
    private TextView tvLogin;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getInsets(WindowInsets.Type.statusBars()).top;
                v.setPadding(0, topInset, 0, 0);
                return insets;
            });
        } else {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getSystemWindowInsetTop();
                v.setPadding(0, topInset, 0, 0);
                return insets.consumeSystemWindowInsets();
            });
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        // Personal Information fields
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // UI components
        btnBackArrow = findViewById(R.id.btnBackArrow);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnApple = findViewById(R.id.btnApple);

        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        // Back button
        btnBackArrow.setOnClickListener(v -> finish());

        // Password visibility toggles
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        // Sign Up button
        btnSignUp.setOnClickListener(v -> handleSignUp());

        // Social login buttons
        btnGoogle.setOnClickListener(v -> handleGoogleLogin());
        btnApple.setOnClickListener(v -> handleAppleLogin());

        // Login link
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private boolean validateFields() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // First Name validation
        if (firstName.isEmpty()) {
            etFirstName.setError("Please enter your first name");
            etFirstName.requestFocus();
            return false;
        }

        // Last Name validation
        if (lastName.isEmpty()) {
            etLastName.setError("Please enter your last name");
            etLastName.requestFocus();
            return false;
        }

        // Email validation
        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Confirm Password validation
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye);
            isConfirmPasswordVisible = true;
        }
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void handleSignUp() {
        if (validateFields()) {
            // Collect data from visible fields
            String firstName = etFirstName.getText().toString().trim();
            String middleName = etMiddleName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Proceed with registration (other fields will be empty)
            performRegistration(firstName, middleName, lastName, email, password);
        }
    }

    private void performRegistration(String firstName, String middleName, String lastName,
                                     String email, String password) {

        // Create Registration object with only filled fields
        Registration registration = new Registration();
        registration.setFirstName(firstName);
        registration.setMiddleName(middleName.isEmpty() ? "" : middleName); // Empty if not provided
        registration.setLastName(lastName);
        registration.setEmailId(email);
        registration.setPasswordHash(password); // Note: Should be hashed on server

        // Set Step 2 fields as empty/default
        registration.setPhoneNumber(""); // Empty phone number
        registration.setAddress(""); // Empty address
        registration.setCityId(0); // Default or 0
        registration.setStateId(0); // Default or 0

        registration.setActive(true);

        // Show loading indicator
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        RegistrationRepository repository = new RegistrationRepository(this);
        repository.registerUser(registration, new RegistrationRepository.RegistrationCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.optBoolean("success", true);
                        String message = response.optString("message", "Account created successfully!");

                        if (success) {
                            new MaterialAlertDialogBuilder(SignUpActivity.this)
                                    .setTitle("Success!")
                                    .setMessage(message)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        navigateToLogin();
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        new MaterialAlertDialogBuilder(SignUpActivity.this)
                                .setTitle("Success!")
                                .setMessage("Account created successfully!")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    navigateToLogin();
                                })
                                .setCancelable(false)
                                .show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(SignUpActivity.this)
                            .setTitle("Error")
                            .setMessage("Sign up failed: " + error)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                });
            }
        });
    }

    private void handleGoogleLogin() {
        // TODO: Implement Google Sign-In
        Toast.makeText(this, "Google Sign-In", Toast.LENGTH_SHORT).show();
    }

    private void handleAppleLogin() {
        // TODO: Implement Apple Sign-In
        Toast.makeText(this, "Apple Sign-In", Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}