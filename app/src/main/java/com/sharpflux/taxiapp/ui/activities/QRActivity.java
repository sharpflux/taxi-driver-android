package com.sharpflux.taxiapp.ui.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.SignalRManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QRActivity extends AppCompatActivity {

    private static final String TAG = "QRActivity";
    private static final String FILE_NAME = "qr_form.png";
    private static final String CHANNEL_ID = "OTP_CHANNEL";

    private ImageView qrCodeImage, btnBack;
    private BottomNavigationView bottomNavigationView;
    private SignalRManager signalRManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        // Notification bar
        Window window = getWindow();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            window.getDecorView().setSystemUiVisibility(0);
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Layout insets
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

        initViews();
        setupListeners();

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        createNotificationChannel();

        int driverId = getDriverId();
        if (driverId == 0) {
            Toast.makeText(this, "Driver ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        File qrFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), FILE_NAME);
        fetchAndSaveQRCode(driverId, qrFile);
    }

    // ─── OTP Listener: register when screen is visible ───────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        registerOtpListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (signalRManager != null) {
            signalRManager.setOtpListener(null);
        }
    }

    private void registerOtpListener() {
        signalRManager = SignalRManager.getInstance();

        signalRManager.setOtpListener(new SignalRManager.OtpListener() {
            @Override
            public void onOtpReceived(int requestId, String otp, int driverId, String timestamp) {
                runOnUiThread(() -> {
                    showOtpDialog(requestId, otp);
                    showOtpNotification(requestId, otp);
                });
            }

            @Override
            public void onConnectionStateChanged(boolean isConnected) {
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() ->
                        Toast.makeText(QRActivity.this,
                                "Connection error: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
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
                    Toast.makeText(QRActivity.this, "OTP copied to clipboard", Toast.LENGTH_SHORT).show();
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
    private void initViews() {
        qrCodeImage = findViewById(R.id.qrCodeImage);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_scanner);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(QRActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_scanner) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(QRActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(QRActivity.this, RidesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private int getDriverId() {
        return getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("user_id", 0);
    }

    private void fetchAndSaveQRCode(int driverId, File file) {
        new Thread(() -> {
            try {
                String apiUrl = APIs.QR_URL.replace("{driverId}", String.valueOf(driverId)) + "?download=true";
                Log.d(TAG, "Fetching QR from: " + apiUrl);

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    if (bitmap != null) {
                        runOnUiThread(() -> qrCodeImage.setImageBitmap(bitmap));
                        saveQRCode(bitmap, file);
                        runOnUiThread(() ->
                                Toast.makeText(this, "QR code loaded", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Failed to decode QR", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching QR", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveQRCode(Bitmap bitmap, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR", e);
        }
    }
}