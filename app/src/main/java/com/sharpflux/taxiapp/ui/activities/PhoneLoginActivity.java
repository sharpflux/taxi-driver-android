package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.network.APIs;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText edtPhoneNumber;
    private Button btnNext;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

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

        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        btnNext = findViewById(R.id.btnNext);
        requestQueue = Volley.newRequestQueue(this);

        // ✅ Handle "Done" button on keyboard
        edtPhoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnNext.performClick();
                    return true;
                }
                return false;
            }
        });

        btnNext.setOnClickListener(v -> {
            String phone = edtPhoneNumber.getText().toString().trim();

            if (phone.isEmpty() || phone.length() != 10) {
                Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            } else {
                sendOtp(phone);
            }
        });
    }

    private void sendOtp(String phone) {
        // ✅ Disable button to prevent multiple clicks
        btnNext.setEnabled(false);
        btnNext.setText("Sending...");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("phoneNumber", phone);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    APIs.SendSMSURL,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // ✅ Re-enable button
                            btnNext.setEnabled(true);
                            btnNext.setText("Next");

                            try {
                                boolean success = response.optBoolean("success", false);
                                String message = response.optString("message", "Something went wrong");

                                if (success) {
                                    Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();

                                    // ✅ Navigate to OTP verification screen
                                    Intent intent = new Intent(PhoneLoginActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(PhoneLoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // ✅ Re-enable button
                            btnNext.setEnabled(true);
                            btnNext.setText("Next");

                            String errorMessage = "Failed to send OTP";
                            if (error.networkResponse != null) {
                                errorMessage += ": " + error.networkResponse.statusCode;
                            } else if (error.getMessage() != null) {
                                errorMessage += ": " + error.getMessage();
                            }
                            Toast.makeText(PhoneLoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            // ✅ Re-enable button
            btnNext.setEnabled(true);
            btnNext.setText("Next");
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
}