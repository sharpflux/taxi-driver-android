package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;

public class HomeActivity extends AppCompatActivity {

    TextView txtWelcome;
    ImageButton btnScanQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtWelcome = findViewById(R.id.txtWelcome);
        btnScanQR = findViewById(R.id.btnScanQR);

        // You can set dynamic welcome message if username is available
        // txtWelcome.setText("Welcome, Amit!");

        // Button click to open QR Scanner activity
        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, ScannerActivity.class);
                startActivity(i);
            }
        });
    }
}
