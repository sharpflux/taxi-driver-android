package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;

public class SplashActivity extends AppCompatActivity {

    // Splash screen display time in milliseconds
    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView logoImage = findViewById(R.id.logoImage);
        TextView appName = findViewById(R.id.appName);
        TextView appTagline = findViewById(R.id.appTagline);

        // Create animations
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);

        // Apply animations
        logoImage.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);
        appTagline.startAnimation(fadeIn);

        // Create a slight scale animation for the logo
        logoImage.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(1000)
                .withEndAction(() ->
                        logoImage.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(500)
                                .start()
                )
                .start();

        // Set version info dynamically (optional)
        TextView versionInfo = findViewById(R.id.versionInfo);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionInfo.setText("Version " + versionName);
        } catch (Exception e) {
            versionInfo.setText("Version 1.0");
        }

        // Delay and navigate to the main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class); // Replace with your main activity
            startActivity(intent);
            finish(); // Close the splash activity so it's not in the back stack
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Smooth transition
        }, SPLASH_DURATION);
    }
}
