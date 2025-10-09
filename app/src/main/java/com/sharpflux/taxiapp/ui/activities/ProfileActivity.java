package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.taxiapp.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePic;
    private TextView tvUserName, tvUserEmail;
    private LinearLayout llPersonalInfo, llPaymentMethods, llNotifications, llHelpSupport, llSettings;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
        loadUserData();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        llPersonalInfo = findViewById(R.id.llPersonalInfo);
        llPaymentMethods = findViewById(R.id.llPaymentMethods);
        llNotifications = findViewById(R.id.llNotifications);
        llHelpSupport = findViewById(R.id.llHelpSupport);
        llSettings = findViewById(R.id.llSettings);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        ivProfilePic.setOnClickListener(v -> {
            // Open image picker or camera
            // TODO: Implement profile picture change
        });

        llPersonalInfo.setOnClickListener(v -> {
            // Navigate to Personal Information screen
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        llPaymentMethods.setOnClickListener(v -> {
            // Navigate to Payment Methods screen
            // Intent intent = new Intent(this, PaymentMethodsActivity.class);
            // startActivity(intent);
        });

        llNotifications.setOnClickListener(v -> {
            // Navigate to Notifications screen
            // Intent intent = new Intent(this, NotificationsActivity.class);
            // startActivity(intent);
        });

        llHelpSupport.setOnClickListener(v -> {
            // Navigate to Help & Support screen
            // Intent intent = new Intent(this, HelpSupportActivity.class);
            // startActivity(intent);
        });

        llSettings.setOnClickListener(v -> {
            // Navigate to Settings screen
            // Intent intent = new Intent(this, SettingsActivity.class);
            // startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_scanner) {
                startActivity(new Intent(ProfileActivity.this, ScannerActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, SignUpActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        // TODO: Load user data from database or shared preferences
        tvUserName.setText("Your Profile");
        tvUserEmail.setText("Manage your account and preferences");
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
    }
}