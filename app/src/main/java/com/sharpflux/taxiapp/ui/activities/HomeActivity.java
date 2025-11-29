package com.sharpflux.taxiapp.ui.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.SignalRManager;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String CHANNEL_ID = "OTP_CHANNEL";

    private SignalRManager signalRManager;
    private int driverId;
    private TextView txtWelcome, txtGreeting;
    private CardView cardScanQR,cardProfileIcon, cardNotifications;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_home);

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

        Log.d(TAG, "==== HomeActivity onCreate ====");

        initViews();
        setupGreeting();
        setupDriverName(); // 👈 Added method for showing driver name
        setupClickListeners();
        setupBottomNavigation();

        driverId = getDriverId();
        Log.d(TAG, "Retrieved Driver ID: " + driverId);

        if (driverId == 0) {
            Log.e(TAG, "Driver ID not found in SharedPreferences");
            Toast.makeText(this, "Driver ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        createNotificationChannel();
        initializeSignalR();
    }

    private void setupDriverName() {
        String driverName = getDriverName();

        // Extract only first name if needed
        if (driverName.contains(" ")) {
            driverName = driverName.split(" ")[0];
        }

        // Capitalize first letter
        if (!driverName.isEmpty()) {
            driverName = driverName.substring(0, 1).toUpperCase() + driverName.substring(1);
        }

        txtWelcome.setText("Welcome back, " + driverName);
    }

    private void initializeSignalR() {
        Log.d(TAG, "==== Initializing SignalR ====");
        signalRManager = SignalRManager.getInstance();

        signalRManager.setOtpListener(new SignalRManager.OtpListener() {
            @Override
            public void onOtpReceived(int requestId, String otp, int driverId, String timestamp) {
                runOnUiThread(() -> {
                    Log.d(TAG, "OTP RECEIVED: " + otp);
                    showOtpDialog(requestId, otp);
                    showOtpNotification(requestId, otp);
                });
            }

            @Override
            public void onConnectionStateChanged(boolean isConnected) {
                runOnUiThread(() -> {
                    String status = isConnected ? "Connected" : "Disconnected";
                    Toast.makeText(HomeActivity.this, "SignalR " + status, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Connection error: " + exception.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        });

        new Thread(() -> {
            try {
                signalRManager.connect(APIs.SIGNALR_HUB_URL, driverId);
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this,
                                "Failed to connect: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showOtpDialog(int requestId, String otp) {
        new AlertDialog.Builder(this)
                .setTitle("🚕 New Bill Request")
                .setMessage("Request ID: " + requestId + "\n\nOTP: " + otp)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Copy OTP", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("OTP", otp);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "OTP copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void showOtpNotification(int requestId, String otp) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🚕 New Ride Request")
                .setContentText("Request ID: " + requestId + " - OTP: " + otp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(requestId, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "OTP Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for new ride OTPs");
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private int getDriverId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getInt("user_id", 0);
    }

    private String getDriverName() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("user_name", "Driver");
    }

    private void initViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        txtGreeting = findViewById(R.id.txtGreeting);
        cardScanQR = findViewById(R.id.cardScanQR);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        cardProfileIcon = findViewById(R.id.cardProfileIcon);
        cardNotifications = findViewById(R.id.cardNotifications);
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good Morning" :
                (hour < 17) ? "Good Afternoon" : "Good Evening";
        txtGreeting.setText(greeting);
    }

    private void setupClickListeners() {
        cardScanQR.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, QRActivity.class)));
        // Profile icon click
        cardProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Notification button click
        cardNotifications.setOnClickListener(v -> {
            // TODO: Implement notifications activity/functionality
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_scanner) {
                startActivity(new Intent(HomeActivity.this, BillRequestActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(HomeActivity.this, RidesActivity.class));
            }
            overridePendingTransition(0, 0);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (signalRManager != null) {
            signalRManager.disconnect();
        }
    }
}
