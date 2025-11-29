package com.sharpflux.taxiapp.ui.activities;

import static com.sharpflux.taxiapp.data.network.APIs.GetPaymentdetails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultListener;
import com.razorpay.PaymentResultWithDataListener;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.network.APIs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private static final String TAG = "PaymentActivity";

    // UI Components
    private EditText etAmount, etName, etPhone;  //etUserId
    private MaterialButton btnProceedPayment;
    private ProgressBar progressBar;
    private String driverName, driverEmail, driverPhone;
    private int driverId;
    private SwipeRefreshLayout swipeRefresh;
    private int selectedPlanId;
    private double finalAmount;



    // Network
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

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
        // Initialize Razorpay
        Checkout.preload(getApplicationContext());

        // Initialize views
        initViews();
        readSelectedPlan();

        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            checkPaymentStatus();
        });


        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(PaymentActivity.this, PricingPlansActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });


        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);

        // Set up click listener
        btnProceedPayment.setOnClickListener(v -> validateAndInitiatePayment());

        checkPaymentStatus();
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        etName = findViewById(R.id.etName);
        //etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);
        progressBar = findViewById(R.id.progressBar);

        // Access SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Retrieve stored values
        String userName = prefs.getString("user_name", "");
        String userEmail = prefs.getString("user_email", "");
        String userPhone = prefs.getString("user_phone", "");
        int userId = prefs.getInt("user_id", 0);

        // Assign values to EditTexts
        etName.setText(userName);
        //etEmail.setText(userEmail);
        etPhone.setText(userPhone);
        etAmount.setText("1"); // default test amount
       // etUserId.setText(userId);


        etName.setEnabled(false);
        //etEmail.setEnabled(false);
        etPhone.setEnabled(false);
        etAmount.setEnabled(false);

        // driver/user ID
        //driverId = userId;

        // log for debugging
        Log.d("PaymentActivity", "Prefilled from SharedPref - Name: " + userName + ", Email: " + userEmail + ", Phone: " + userPhone);
    }

    private void readSelectedPlan() {
        Intent i = getIntent();

        selectedPlanId = i.getIntExtra("planId", 0);
        String planName = i.getStringExtra("planName");
        double originalPrice = i.getDoubleExtra("originalPrice", 0.0);
        double discountedPrice = i.getDoubleExtra("discountedPrice", 0.0);
        int discountPercent = i.getIntExtra("discountPercent", 0);

        finalAmount = i.getDoubleExtra("finalAmount", 0.0);

        Log.d("PAYMENT_PLAN", "Plan Selected: " + planName);
        Log.d("PAYMENT_PLAN", "Amount to Pay: " + finalAmount);

        // Show on screen
        etAmount.setText(String.valueOf(finalAmount));
    }
    private void validateAndInitiatePayment() {
        // Get input values
        String amountStr = etAmount.getText().toString().trim();
        String name = etName.getText().toString().trim();
        //String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Enter amount");
            etAmount.requestFocus();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            etAmount.setError("Amount must be greater than 0");
            etAmount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }

//        if (TextUtils.isEmpty(email)) {
//            etEmail.setError("Enter your email");
//            etEmail.requestFocus();
//            return;
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            etEmail.setError("Enter valid email");
//            etEmail.requestFocus();
//            return;
//        }

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            etPhone.setError("Enter valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);
        // All validations passed, proceed with payment
        createOrderOnServer((int) amount, name, phone,userId,selectedPlanId);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnProceedPayment.setEnabled(!show);

        if (show) {
            btnProceedPayment.setText("Processing...");
        } else {
            btnProceedPayment.setText("Proceed to Payment");
        }
    }

    private void createOrderOnServer(int amountInRupees, String customerName,
                                      String customerContact,int userId,int selectedPlanId) {
        showLoading(true);

        String url = APIs.RazorPayCreateOrder;

        try {
            JSONObject body = new JSONObject();
            body.put("amount", amountInRupees);
            body.put("userId", userId);

            // 🟢 Add driverId from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            int driverId = prefs.getInt("user_id", 0);
            if (driverId == 0) {
                Log.w(TAG, "⚠ driverId not found in SharedPreferences");
            }
            body.put("driverId", driverId);
            body.put("planId", selectedPlanId);


            Log.d(TAG, "Creating order with amount: " + amountInRupees + " for driverId: " + driverId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        showLoading(false);
                        try {
                            String orderId = response.getString("orderId");
                            String key = response.getString("key");
                            int amountPaise = response.getInt("amount");

                            Log.d(TAG, "Order created successfully. OrderId: " + orderId);

                            // Start Razorpay checkout
                            startPayment(key, orderId, amountPaise, customerName,
                                     customerContact);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order response", e);
                            Toast.makeText(PaymentActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        showLoading(false);
                        String errorMsg = "Failed to create order";

                        if (error.networkResponse != null) {
                            errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e(TAG, "Error response: " + responseBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading response", e);
                            }
                        }

                        Log.e(TAG, "Order creation failed", error);
                        Toast.makeText(PaymentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(request);

        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Exception creating order request", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startPayment(String key, String orderId, int amountPaise,
                              String customerName, String customerContact) {
        try {
            Checkout checkout = new Checkout();
            checkout.setKeyID(key);

            JSONObject options = new JSONObject();
            options.put("name", "TaxiApp");
            options.put("description", "Ride Payment");
            options.put("order_id", orderId);
            options.put("currency", "INR");
            options.put("amount", amountPaise);

            // Prefill customer details
            JSONObject prefill = new JSONObject();
            prefill.put("name", customerName);
            //prefill.put("email", customerEmail);
            prefill.put("contact", customerContact);
            options.put("prefill", prefill);

            // Theme customization
            JSONObject theme = new JSONObject();
            theme.put("color", "#6200EE");
            options.put("theme", theme);

            Log.d(TAG, "Opening Razorpay checkout");
            checkout.open(this, options);

        } catch (Exception e) {
            Log.e(TAG, "Error starting Razorpay checkout", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData) {
        try {
            Log.d("PAY_SUCCESS", "Payment successful: " + razorpayPaymentID);
            Log.d("PAYMENT_DATA", "Payment Data: " + paymentData);

            // Get full payment details
            String orderId = paymentData.getOrderId();
            String paymentId = paymentData.getPaymentId();
            String signature = paymentData.getSignature();

            // Verify on server
            verifyPaymentOnServer(orderId, paymentId, signature,selectedPlanId);

        } catch (Exception e) {
            Log.e("PAY_ERROR", "Error processing payment success", e);
            Toast.makeText(this, "Payment success but verification pending", Toast.LENGTH_LONG).show();
        }
    }

    private void verifyPaymentOnServer(String orderId, String paymentId, String signature,int selectedPlanId) {
        showLoading(true);

        String url = APIs.RazorPayVerification;

        try {
            JSONObject body = new JSONObject();
            body.put("RazorpayOrderId", orderId);
            body.put("RazorpayPaymentId", paymentId);
            body.put("RazorpaySignature", signature);
            body.put("planId", selectedPlanId);


            // 🟢 Get driverId and token from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            int driverId = prefs.getInt("user_id", 0);
            String token = prefs.getString("authToken", "");

            if (driverId == 0) {
                Log.w("VERIFY_PAY", "⚠ driverId not found in SharedPreferences");
            }

            if (token.isEmpty()) {
                Log.w("VERIFY_PAY", "⚠ JWT token not found in SharedPreferences");
            }

            body.put("driverId", driverId);

            Log.d("VERIFY_PAY", "Verifying payment with orderId: " + orderId + " and driverId: " + driverId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        showLoading(false);
                        try {
                            Log.d("VERIFY_PAY_RESPONSE", "Full response: " + response.toString());

                            String status = response.optString("status");
                            String message = response.optString("message");

                            if ("success".equalsIgnoreCase(status)) {
                                Toast.makeText(this, "✓ " + message, Toast.LENGTH_LONG).show();

                                JSONObject paymentDetails = response.optJSONObject("paymentDetails");
                                if (paymentDetails != null) {
                                    Log.d("RAZORPAY_FULL_PAYMENT", paymentDetails.toString(4));
                                    // Handle successful verification logic
                                }

                                Intent intent = new Intent(this, VerificationCheckActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(this, "⚠ " + message, Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            Log.e("VERIFY_PAY", "Error parsing verification response", e);
                            Toast.makeText(this, "Verification response error", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        showLoading(false);
                        Log.e("VERIFY_PAY", "Payment verification failed", error);
                        Toast.makeText(this, "Verification failed. Please contact support.", Toast.LENGTH_LONG).show();
                    }
            ) {
                // Authorization header (Bearer token)
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(request);

        } catch (Exception e) {
            showLoading(false);
            Log.e("VERIFY_PAY", "Exception during verification", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onPaymentError(int code, String response, PaymentData paymentData) {
        Log.e(TAG, "Payment error. Code: " + code + ", Response: " + response);

        String message = "Payment failed. Please try again.";

        try {
            if (response != null && response.trim().startsWith("{")) {
                JSONObject errorObj = new JSONObject(response);
                if (errorObj.has("error")) {
                    JSONObject err = errorObj.getJSONObject("error");
                    message = err.optString("description", message);
                } else {
                    message = errorObj.optString("description", message);
                }
            } else if (response != null && !response.isEmpty()) {
                // Handle plain text message
                message = response;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing payment error response", e);
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void checkPaymentStatus() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driverId = prefs.getInt("user_id", 0);

        swipeRefresh.setRefreshing(true);

        String url = GetPaymentdetails + driverId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {

                            JSONObject obj = response.getJSONObject(0);
                            String status = obj.getString("Status");

                            Log.d("PAYMENT_STATUS", "Status: " + status);

                            if (status.equalsIgnoreCase("captured")) {

                                Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();

                                // Redirect automatically
                                Intent i = new Intent(this, VerificationCheckActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }
                            else {
                                Toast.makeText(this, "Payment pending. Please complete payment.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(this, "No payment found. Please make a payment.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    swipeRefresh.setRefreshing(false);
                },
                error -> {
                    Log.e("PAYMENT_STATUS", "Error fetching payment", error);
                    Toast.makeText(this, "Error checking payment status", Toast.LENGTH_LONG).show();
                    swipeRefresh.setRefreshing(false);
                }
        );

        queue.add(request);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}