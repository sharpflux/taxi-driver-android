package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.network.APIs;

import org.json.JSONObject;

public class VerificationCheckActivity extends AppCompatActivity {

    private static final String TAG = "VerificationCheck";
    String url = APIs.GetDriverDocument;   // your API base URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_pending);

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

        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            checkVerificationStatus();
        });

        // Call once when activity opens
        checkVerificationStatus();
    }

    private void checkVerificationStatus() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driverId = prefs.getInt("user_id", 0);

        if (driverId == 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl = url + "/" + driverId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {
                    try {

                        if (response.length() == 0) {
                            Toast.makeText(this, "No payment data found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject obj = response.getJSONObject(0);

                        String paymentStatus = obj.optString("PaymentStatus", "");
                        int statusId = obj.optInt("StatusId", 0);

                        Log.d(TAG, "paymentStatus = " + paymentStatus);
                        Log.d(TAG, "statusId = " + statusId);

//                        // ---------------------------------------------------
//                        // FIRST CHECK PAYMENT STATUS
//                        // ---------------------------------------------------
//                        if (!paymentStatus.equalsIgnoreCase("Captured")) {
//
//                            // Payment not completed → redirect to PricingPlansActivity
//                            Intent intent = new Intent(this, PricingPlansActivity.class);
//                            startActivity(intent);
//                            finish();
//                            return; // stop further execution
//                        }
                        if(statusId==5){
                            Intent intent = new Intent(this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                            return; // stop further execution
                        }

                        // ---------------------------------------------------
                        // 2️⃣ PAYMENT CAPTURED → NOW CHECK STATUS ID
                        // ---------------------------------------------------
                        switch (statusId) {

                            case 2: // Document upload required
                                startActivity(new Intent(this, DocumentUploadActivity.class));
                                finish();
                                break;

                            case 5: // Verified → Go to Home
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                                break;

                            default:
                                Toast.makeText(this, "Verification still pending...", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error checking status", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

}
