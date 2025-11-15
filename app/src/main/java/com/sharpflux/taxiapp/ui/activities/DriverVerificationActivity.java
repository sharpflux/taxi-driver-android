package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.sharpflux.taxiapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DriverVerificationActivity extends AppCompatActivity {

    private MaterialCardView cardVerification, cardPayment;
    private TextView tvTitle;
    private CircularProgressIndicator progressIndicator;
    private RequestQueue requestQueue;

    private static final String BASE_URL = "https://yourapi.com/api/Driver/VerificationStatus"; // 🔹 replace with your actual API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_status);

        tvTitle = findViewById(R.id.tvTitle);
        cardVerification = findViewById(R.id.cardVerification);
        cardPayment = findViewById(R.id.cardPayment);
//        progressIndicator = findViewById(R.id.progressIndicator);

        requestQueue = Volley.newRequestQueue(this);

        // Example: get driverId from SharedPreferences or token
        int driverId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("driverId", 0);

        fetchVerificationStatus(driverId);
    }

    private void fetchVerificationStatus(int driverId) {
        progressIndicator.setVisibility(View.VISIBLE);

        String url = BASE_URL + "?driverId=" + driverId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressIndicator.setVisibility(View.GONE);
                        try {
                            int verificationStatus = response.optInt("verificationStatus");
                            String rejectionReason = response.optString("rejectionReason");

                            updateUI(verificationStatus, rejectionReason);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressIndicator.setVisibility(View.GONE);
                        tvTitle.setText("Error loading status. Try again.");
                    }
                });

        requestQueue.add(request);
    }

    private void updateUI(int status, String reason) {
        /*
         * status codes:
         * 0 = Pending
         * 1 = Accepted
         * 2 = Rejected
         * 3 = Modify Required
         */

        switch (status) {
            case 0: // Pending
                cardVerification.setVisibility(View.VISIBLE);
                cardPayment.setVisibility(View.GONE);
                tvTitle.setText("Verification Pending");
                break;

            case 1: // Accepted
                tvTitle.setText("Verification Successful");
                startActivity(new Intent(this, HomeActivity.class)); // Redirect to Home
                finish();
                break;

            case 2: // Rejected
                cardVerification.setVisibility(View.VISIBLE);
                cardPayment.setVisibility(View.GONE);
                tvTitle.setText("Verification Rejected: " + reason);
                break;

            case 3: // Modification
                cardVerification.setVisibility(View.VISIBLE);
                cardPayment.setVisibility(View.GONE);
                tvTitle.setText("Please modify your details and resubmit.");
                break;

            default:
                tvTitle.setText("Unknown status");
                break;
        }
    }
}
