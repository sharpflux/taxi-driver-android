package com.sharpflux.taxiapp.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.network.APIs;
//import com.sharpflux.taxiapp.utils.AppSignatureHelper;
import com.sharpflux.taxiapp.utils.SessionManager;
import com.sharpflux.taxiapp.utils.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpVerificationActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4;
    TextView tvPhoneNumber, tvResend, tvDidntReceive;
    Button btnVerify;
    ImageView imgBack;

    String phoneNumber;
    RequestQueue requestQueue;
    UserPreferences prefsHelper;

    // Timer variables
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private static final long TIMER_DURATION = 60000; // 60 seconds

    // Flag to prevent multiple API calls
    private boolean isVerifying = false;

    // SMS Retriever
    private SmsBroadcastReceiver smsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

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

//        // In onCreate() - Run this once to get your hash
//        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
//        Log.d("APP_HASH", "Hash: " + appSignatureHelper.getAppSignatures());

        // Initialize views
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvResend = findViewById(R.id.tvResend);
        tvDidntReceive = findViewById(R.id.tvDidntReceive);
        btnVerify = findViewById(R.id.btnVerify);
        imgBack = findViewById(R.id.imgBack);

        // Initialize request queue and preferences helper
        requestQueue = Volley.newRequestQueue(this);
        prefsHelper = new UserPreferences(this);

        // Get phone number from intent
        phoneNumber = getIntent().getStringExtra("phone");
        tvPhoneNumber.setText("Code sent to " + phoneNumber);

        // Back button
        imgBack.setOnClickListener(v -> onBackPressed());

        // Verify button click
        btnVerify.setOnClickListener(v -> verifyOtp());

        // Setup OTP input listeners
        setupOtpInputs();

        // Start timer
        startTimer();

        // Initially disable verify button
        btnVerify.setEnabled(false);
        btnVerify.setAlpha(0.5f);

        // Setup resend click listener (initially not clickable)
        tvDidntReceive.setOnClickListener(v -> {
            if (!timerRunning) {
                resendOtp();
            }
        });

        // Focus on first OTP field
        otp1.requestFocus();

        // Start SMS Retriever
        startSmsRetriever();
    }

    /**
     * Start SMS Retriever API to auto-read OTP
     */
    private void startSmsRetriever() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, now wait for SMS
                registerBroadcastReceiver();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever
                Toast.makeText(OtpVerificationActivity.this,
                        "Auto OTP detection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Register broadcast receiver for SMS
     */
    private void registerBroadcastReceiver() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.setOtpListener(otp -> {
            if (otp != null && otp.length() == 4) {
                fillOtpFields(otp);
            }
        });

        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);
    }


    /**
     * Fill OTP fields automatically
     */
    private void fillOtpFields(String otp) {
        if (otp.length() >= 4) {
            otp1.setText(String.valueOf(otp.charAt(0)));
            otp2.setText(String.valueOf(otp.charAt(1)));
            otp3.setText(String.valueOf(otp.charAt(2)));
            otp4.setText(String.valueOf(otp.charAt(3)));

            // The TextWatcher will automatically trigger verification
        }
    }

    /**
     * Setup OTP input fields with auto-focus and text watchers
     */
    private void setupOtpInputs() {
        // Add text watchers to auto-move focus
        otp1.addTextChangedListener(new OtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OtpTextWatcher(otp4, null));

        // Add watcher to check if all fields are filled and auto-verify
        TextWatcher verifyButtonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkOtpFilledAndAutoVerify();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        otp1.addTextChangedListener(verifyButtonWatcher);
        otp2.addTextChangedListener(verifyButtonWatcher);
        otp3.addTextChangedListener(verifyButtonWatcher);
        otp4.addTextChangedListener(verifyButtonWatcher);
    }

    /**
     * Check if all OTP fields are filled, enable verify button, and auto-click after delay
     */
    private void checkOtpFilledAndAutoVerify() {
        boolean allFilled = !otp1.getText().toString().trim().isEmpty() &&
                !otp2.getText().toString().trim().isEmpty() &&
                !otp3.getText().toString().trim().isEmpty() &&
                !otp4.getText().toString().trim().isEmpty();

        if (allFilled && timerRunning) {
            btnVerify.setEnabled(true);
            btnVerify.setAlpha(1.0f);

            // Auto-click verify button after 300ms delay
            new Handler().postDelayed(() -> {
                if (!isVerifying && allFilled) {
                    btnVerify.performClick();
                }
            }, 300);
        } else {
            btnVerify.setEnabled(false);
            btnVerify.setAlpha(0.5f);
        }
    }

    /**
     * Start countdown timer
     */
    private void startTimer() {
        timerRunning = true;
        tvDidntReceive.setEnabled(false);
        tvDidntReceive.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                tvResend.setText("Resend code in " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                tvResend.setText("Time expired");

                // Disable verify button
                btnVerify.setEnabled(false);
                btnVerify.setAlpha(0.5f);

                // Enable resend option
                tvDidntReceive.setEnabled(true);
                tvDidntReceive.setAlpha(1.0f);
                tvDidntReceive.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }.start();
    }

    /**
     * Resend OTP
     */
    private void resendOtp() {
        // Clear OTP fields
        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp1.requestFocus();

        // Reset resend text color
        int textSecondaryColor = getResources().getColor(android.R.color.darker_gray);
        tvDidntReceive.setTextColor(textSecondaryColor);

        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();

        // Restart timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startTimer();

        // Restart SMS Retriever
        startSmsRetriever();

        // Call your resend OTP API
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("phoneNumber", phoneNumber);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    APIs.SendSMSURL,
                    jsonBody,
                    response -> {
                        Toast.makeText(this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
                    }
            );

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inner class for OTP text watcher to handle auto-focus
     */
    private class OtpTextWatcher implements TextWatcher {
        private EditText currentView;
        private EditText nextView;

        OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            } else if (s.length() == 0 && before == 1) {
                // Handle backspace - move to previous field
                if (currentView == otp2) otp1.requestFocus();
                else if (currentView == otp3) otp2.requestFocus();
                else if (currentView == otp4) otp3.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Unregister broadcast receiver
        if (smsBroadcastReceiver != null) {
            try {
                unregisterReceiver(smsBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void verifyOtp() {
        // Prevent multiple simultaneous API calls
        if (isVerifying) {
            return;
        }

        String otpCode = otp1.getText().toString().trim() +
                otp2.getText().toString().trim() +
                otp3.getText().toString().trim() +
                otp4.getText().toString().trim();

        if (otpCode.length() != 4) {
            Toast.makeText(this, "Please enter a valid 4-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set verifying flag and disable button
        isVerifying = true;
        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("phoneNumber", phoneNumber);
            requestBody.put("otpCode", otpCode);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIs.VerifySMSURL,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Reset verifying flag
                            isVerifying = false;
                            btnVerify.setText("Verify");

                            try {
                                String status = response.optString("status", "");
                                boolean success = response.optBoolean("success", false);

                                if (!success) {
                                    // Handle failed verification
                                    String message = response.optString("message", "Invalid or expired OTP");
                                    Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show();

                                    // Clear OTP fields and re-enable button
                                    otp1.setText("");
                                    otp2.setText("");
                                    otp3.setText("");
                                    otp4.setText("");
                                    otp1.requestFocus();
                                    return;
                                }

                                switch (status) {

                                    case "Verified":
                                        // Save user session
                                        saveUserSession(response);

                                        Toast.makeText(OtpVerificationActivity.this,
                                                "Login successful", Toast.LENGTH_SHORT).show();

                                        // Get saved driverId
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        int driverId = prefs.getInt("user_id", 0);

                                        if (driverId == 0) {
                                            Intent onboarding = new Intent(OtpVerificationActivity.this, OnboardingActivity.class);
                                            startActivity(onboarding);
                                            finish();
                                            break;
                                        }

                                        String url = APIs.GetDriverDocument + "/" + driverId;
                                        Log.d("CHECK_API_URL", url);

                                        JsonArrayRequest docRequest = new JsonArrayRequest(
                                                Request.Method.GET,
                                                url,
                                                null,
                                                docResponse -> {
                                                    try {
                                                        String paymentStatus = "";
                                                        int statusId = 0;

                                                        if (docResponse.length() > 0) {
                                                            JSONObject obj = docResponse.getJSONObject(0);
                                                            paymentStatus = obj.optString("PaymentStatus", "");
                                                            statusId = obj.optInt("StatusId", 0);
                                                        }

                                                        // Clean string
                                                        paymentStatus = paymentStatus == null ? "" : paymentStatus.trim();
                                                        Log.d("PAYMENT_STATUS", "Value = [" + paymentStatus + "]");

                                                        if (paymentStatus.equalsIgnoreCase("captured")) {

                                                            // Payment done → go to verification check
                                                            Intent i = new Intent(OtpVerificationActivity.this, VerificationCheckActivity.class);
                                                            startActivity(i);
                                                            finish();
                                                            return;
                                                        } else {

                                                            // Payment NOT done → go to pricing plan
                                                            Intent i = new Intent(OtpVerificationActivity.this, PricingPlansActivity.class);
                                                            startActivity(i);
                                                            finish();
                                                            return;
                                                        }

                                                    } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                        // On error → take user to verification check
                                                        startActivity(new Intent(OtpVerificationActivity.this, VerificationCheckActivity.class));
                                                        finish();
                                                    }
                                                },
                                                error -> {
                                                    // API error → go to verification check
                                                    startActivity(new Intent(OtpVerificationActivity.this, VerificationCheckActivity.class));
                                                    finish();
                                                }
                                        );

                                        Volley.newRequestQueue(OtpVerificationActivity.this).add(docRequest);
                                        break;


                                    case "Verified_But_No_Driver":
                                        Toast.makeText(OtpVerificationActivity.this,
                                                "Please complete registration", Toast.LENGTH_SHORT).show();

                                        Intent registerIntent = new Intent(OtpVerificationActivity.this, DriverRegistrationActivity.class);
                                        registerIntent.putExtra("phone", phoneNumber);
                                        registerIntent.putExtra("isPhoneVerified", true);
                                        startActivity(registerIntent);
                                        finish();
                                        break;


                                    default:
                                        Toast.makeText(OtpVerificationActivity.this,
                                                "Unexpected response. Please try again.", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(OtpVerificationActivity.this,
                                        "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Reset verifying flag and re-enable button
                            isVerifying = false;
                            btnVerify.setEnabled(true);
                            btnVerify.setText("Verify");

                            String errorMessage = "Server error";
                            if (error.networkResponse != null) {
                                errorMessage += ": " + error.networkResponse.statusCode;
                            } else if (error.getMessage() != null) {
                                errorMessage += ": " + error.getMessage();
                            }
                            Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            requestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            isVerifying = false;
            btnVerify.setEnabled(true);
            btnVerify.setText("Verify");
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save user session data to SharedPreferences
     */
    private void saveUserSession(JSONObject response) {
        try {
            // Extract token data
            String authToken = response.optString("authToken", "");
            String expiresIn = response.optString("expiresIn", "");
            int driverId = response.optInt("driverId", -1);

            // Save basic session data using helper
            prefsHelper.saveOTPVerificationSession(authToken, expiresIn, driverId, phoneNumber);

            // Extract and save user details
            JSONObject user = response.optJSONObject("user");

            if (user != null) {
                prefsHelper.saveUserDetails(
                        user.optInt("id", -1),
                        user.optString("name", ""),
                        user.optString("email", ""),
                        user.optString("phoneNumber", phoneNumber),
                        user.optBoolean("isActive", false),
                        user.optInt("roleId", -1),
                        user.optInt("locationId", -1),
                        user.optString("roleName", ""),
                        user.optString("companyLogoURL", ""),
                        user.optInt("statusId",0)
                );

                // Also create session using SessionManager (if you're using it)
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.createLoginSession(
                        user.optString("email", ""),
                        user.optString("email", ""),
                        String.valueOf(user.optInt("id", -1)),
                        user.optString("name", "")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving session", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Broadcast Receiver to receive SMS
     */
    public static class SmsBroadcastReceiver extends BroadcastReceiver {

        private OtpReceivedListener otpListener;

        public void setOtpListener(OtpReceivedListener listener) {
            this.otpListener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // Get SMS message contents
                        String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);

                        // Extract OTP from message
                        if (message != null && otpListener != null) {
                            String otp = extractOtp(message);
                            if (otp != null) {
                                otpListener.onOtpReceived(otp);
                            }
                        }
                        break;

                    case CommonStatusCodes.TIMEOUT:
                        // Timeout waiting for SMS
                        break;
                }
            }
        }

        /**
         * Extract OTP code from SMS message
         */
        private String extractOtp(String message) {
            // Pattern to match 4-6 digit OTP
            Pattern pattern = Pattern.compile("(\\d{4,6})");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                return matcher.group(0);
            }
            return null;
        }

        public interface OtpReceivedListener {
            void onOtpReceived(String otp);
        }
    }
}