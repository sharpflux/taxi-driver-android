package com.sharpflux.taxiapp.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import androidx.activity.viewModels;
import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new AuthViewModel(); // simple init

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            viewModel.login(user, pass).observe(this, success -> {
                if (success) {
                    Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show();
                    // TODO: navigate to HomeActivity
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
