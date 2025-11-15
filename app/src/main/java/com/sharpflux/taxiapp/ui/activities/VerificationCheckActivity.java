package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;

public class VerificationCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int verificationStatus = prefs.getInt("verificationStatus", 1);
//        int verificationStatus = 1;

        // If verified → go directly to Home
        if (verificationStatus == 5) {
            Intent intent = new Intent(VerificationCheckActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Else show verification pending screen
        setContentView(R.layout.activity_verification_pending);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> {


            if (verificationStatus == 5) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Verification still pending...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAgain() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int verificationStatus = prefs.getInt("verificationStatus", 1);

        if (verificationStatus == 5) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Verification pending...", Toast.LENGTH_SHORT).show();
        }
    }
}
