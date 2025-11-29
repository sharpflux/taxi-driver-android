package com.sharpflux.taxiapp.ui.activities;

import static com.sharpflux.taxiapp.data.network.APIs.GetDriverDocument;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.sharpflux.logomobility.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
        // Delay for splash
        new Handler().postDelayed(this::checkPaymentStatus, 1500);
    }

    private void checkPaymentStatus() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driverId = prefs.getInt("user_id", 0);

        if (driverId == 0) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        String url = GetDriverDocument + "/" + driverId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String paymentStatus = "";
                        int statusId = -1;

                        if (response.length() > 0) {
                            JSONObject obj = response.getJSONObject(0);
                            paymentStatus = obj.optString("PaymentStatus", "");
                            statusId = obj.optInt("statusId", -1);
                        }

                        Log.d("PaymentStatus", "PaymentStatus = " + paymentStatus);
                        Log.d("StatusId", "statusId = " + statusId);

                        // Check statusId first
                        if (statusId == 5) {
                            // Navigate directly to Home screen
                            navigate(HomeActivity.class);
                        } else if (paymentStatus.equalsIgnoreCase("captured")) {
                            navigate(VerificationCheckActivity.class);
                        } else {
                            navigate(PricingPlansActivity.class);
                        }

                    } catch (JSONException e) {
                        Log.e("PaymentStatus", "Error parsing JSON: " + e.getMessage());
                        // Handle error - maybe navigate to a default screen
                        navigate(PricingPlansActivity.class);
                    }
                },
                error -> {
                    navigate(PricingPlansActivity.class);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void navigate(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish();
    }
}