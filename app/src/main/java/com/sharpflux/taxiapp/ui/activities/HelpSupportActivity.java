package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.sharpflux.taxiapp.R;

public class HelpSupportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView btnVisitSupport;
    private static final String SUPPORT_URL = "https://logomobility.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpsupport);

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

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnVisitSupport = findViewById(R.id.btnVisitSupport);

        // Back button click listener
        btnBack.setOnClickListener(v -> onBackPressed());

        // Visit Support Website button click listener
        btnVisitSupport.setOnClickListener(v -> openSupportWebsite());
    }

    private void openSupportWebsite() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open browser", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}