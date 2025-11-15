package com.sharpflux.taxiapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePic;
    private TextView tvUserName, tvUserEmail;
    private LinearLayout llPersonalInfo, llPaymentMethods, llNotifications, llHelpSupport, llSettings, llLogout;
    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        sessionManager = new SessionManager(this);
        initViews();
        setupListeners();
        loadUserData();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        //tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        llPersonalInfo = findViewById(R.id.llPersonalInfo);
        llPaymentMethods = findViewById(R.id.llPaymentMethods);
        llNotifications = findViewById(R.id.llNotifications);
        llHelpSupport = findViewById(R.id.llHelpSupport);
        llSettings = findViewById(R.id.llSettings);
        llLogout = findViewById(R.id.llLogout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        ivProfilePic.setOnClickListener(v -> {
            // Open image picker or camera
            // TODO: Implement profile picture change
        });

        llPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        llPaymentMethods.setOnClickListener(v -> {
            // Navigate to Razorpay Payment screen
            Intent intent = new Intent(ProfileActivity.this, PaymentActivity.class);

            // You can also pass data if needed (for example, the ride amount, user info, etc.)
            intent.putExtra("amount", 200); // ₹200 — replace this dynamically as needed
            intent.putExtra("customerName", sessionManager.getUsername());
            intent.putExtra("customerEmail", sessionManager.getUserEmail());
            intent.putExtra("customerContact", "9999999999"); // dummy contact or from session

            startActivity(intent);
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

        llLogout.setOnClickListener(v -> showLogoutDialog());
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
                startActivity(new Intent(ProfileActivity.this, QRActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, BillRequestActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        // Load user data from session
        String username = sessionManager.getUsername();
        String email = sessionManager.getUserEmail();

//        if (username != null && !username.isEmpty()) {
//            tvUserName.setText(username);
//        } else {
//            tvUserName.setText("Your Profile");
//        }

        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText(email);
        } else {
            tvUserEmail.setText("Manage your account and preferences");
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear session
                    sessionManager.logout();

                    // Navigate to login screen
                    Intent intent = new Intent(ProfileActivity.this, PhoneLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        loadUserData(); // Refresh user data when returning to screen
    }
}
