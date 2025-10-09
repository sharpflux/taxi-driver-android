package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.VolleyClient;

import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

public class BillActivity extends AppCompatActivity {

    private TextInputEditText etDriverName, etMobileNo, etVehicleNo, etPickFrom, etDropAt, etDistance, etFare;
    private Button btnSubmit;

    // Hidden field (not in UI)
    private String driverId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        etDriverName = findViewById(R.id.etDriverName);
        etMobileNo = findViewById(R.id.etMobileNo);
        etVehicleNo = findViewById(R.id.etVehicleNo);
        etPickFrom = findViewById(R.id.etPickFrom);
        etDropAt = findViewById(R.id.etDropAt);
        etDistance = findViewById(R.id.etDistance);
        etFare = findViewById(R.id.etFare);
        btnSubmit = findViewById(R.id.btnSubmit);

        // ✅ Get scanned QR data
        String scannedData = getIntent().getStringExtra("SCANNED_DATA");
        if (scannedData != null) {
            bindScannedData(scannedData);
        }

        btnSubmit.setOnClickListener(v -> saveBill());
    }

    private void bindScannedData(String scannedData) {
        try {
            // Split into lines
            String[] lines = scannedData.split("\n");
            for (String line : lines) {
                String[] keyValue = line.split(":", 2); // split only into 2 parts
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().toLowerCase();
                    String value = keyValue[1].trim();

                    switch (key) {
                        case "driver id":
                            driverId = value; // hidden, don't bind to UI
                            break;
                        case "name":
                            etDriverName.setText(value);
                            break;
                        case "phone":
                            etMobileNo.setText(value);
                            break;
                        case "vehicle":
                            etVehicleNo.setText(value);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse QR data", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveBill() {
        try {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            int customerId = prefs.getInt("user_id", -1);
            String authToken = prefs.getString("authToken", "");

            if (customerId == -1) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("driverId", driverId); // ✅ Hidden but sent in payload
            jsonBody.put("customersId", customerId);
           // jsonBody.put("driverName", etDriverName.getText().toString().trim());
           // jsonBody.put("mobileNo", etMobileNo.getText().toString().trim());
           // jsonBody.put("vehicleNo", etVehicleNo.getText().toString().trim());
            jsonBody.put("pickFrom", etPickFrom.getText().toString().trim());
            jsonBody.put("dropAt", etDropAt.getText().toString().trim());
            jsonBody.put("distance", etDistance.getText().toString().trim());
            jsonBody.put("fare", etFare.getText().toString().trim());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIs.SaveBillURL,
                    jsonBody,
                    response -> {
                        Toast.makeText(BillActivity.this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show();
                        showSuccessDialog();
                        finish();
                    },
                    error -> {
                        Toast.makeText(BillActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            ) {
                // ✅ Attach Authorization Header (Bearer Token)
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json");
                    if (!authToken.isEmpty()) {
                        headers.put("Authorization", "Bearer " + authToken);
                    }
                    return headers;
                }
            };

            VolleyClient.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success")
                .setMessage("Bill has been saved successfully.")
                .setCancelable(false) // Prevent closing by tapping outside
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // ✅ Go to HomeActivity
                    Intent intent = new Intent(BillActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Close current screen
                })
                .show();
    }
}

