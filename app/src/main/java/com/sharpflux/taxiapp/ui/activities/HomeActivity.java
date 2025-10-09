package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.taxiapp.R;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private TextView txtWelcome, txtGreeting;
    private CardView cardScanQR;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupGreeting();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        txtGreeting = findViewById(R.id.txtGreeting);
        cardScanQR = findViewById(R.id.cardScanQR);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        txtGreeting.setText(greeting);

        // You can set dynamic welcome message if username is available
        // String username = getIntent().getStringExtra("username");
        // txtWelcome.setText("Welcome, " + username + "!");
    }

    private void setupClickListeners() {
        cardScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ScannerActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_scanner) {
                startActivity(new Intent(HomeActivity.this, ScannerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(HomeActivity.this, SignUpActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}