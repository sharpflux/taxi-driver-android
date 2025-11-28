package com.sharpflux.taxiapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.utils.SessionManager;
import com.sharpflux.taxiapp.utils.UserPreferences;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivProfilePic;
    private TextView tvUserName, tvUserEmail;
    private LinearLayout llPersonalInfo, llPaymentMethods, llNotifications, llHelpSupport, llSettings, llLogout;
    private BottomNavigationView bottomNavigationView;

    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //notification bar
        Window window = getWindow();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            window.getDecorView().setSystemUiVisibility(0); // remove light icons flag
        } else {
            // Light mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //layout
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

        userPreferences = new UserPreferences(this);

        initViews();
        setupListeners();
        loadUserData();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserName = findViewById(R.id.tvUserName);
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

        llPersonalInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });

        llPaymentMethods.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PricingPlansActivity.class);
//            intent.putExtra("amount", 200);
//            intent.putExtra("customerName", userPreferences.getUserName());
//            intent.putExtra("customerEmail", userPreferences.getUserEmail());
//            intent.putExtra("customerContact", userPreferences.getUserPhone());
            startActivity(intent);
        });

        llHelpSupport.setOnClickListener(v -> startActivity(new Intent(this, HelpSupportActivity.class)));
        llSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        llLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        String email = userPreferences.getUserEmail();
        String name = userPreferences.getUserName();

        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        } else {
            tvUserName.setText("Your Profile");
        }

        if (email != null && !email.isEmpty()) {
            tvUserEmail.setText(email);
        } else {
            tvUserEmail.setText("Manage your account and preferences");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_scanner) {
                startActivity(new Intent(ProfileActivity.this, BillRequestActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, RidesActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {

                    // ✅ Clear session using UserPreferences
                    userPreferences.clearSession();

                    // Go to login
                    Intent intent = new Intent(ProfileActivity.this, PhoneLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        loadUserData();
    }
}