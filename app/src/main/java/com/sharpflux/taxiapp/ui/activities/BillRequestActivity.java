package com.sharpflux.taxiapp.ui.activities;

import static com.sharpflux.taxiapp.data.network.APIs.UpdateBillApprovalURL;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.BillRequest;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.VolleyClient;
import com.sharpflux.taxiapp.ui.adapter.BillRequestAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

public class BillRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillRequestAdapter adapter;
    private List<BillRequest> billRequestList;
    private ProgressBar progressBar;
    private OnApprovalActionListener approvalActionListener;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_request);

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
        //Layout
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Requests");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        billRequestList = new ArrayList<>();
        adapter = new BillRequestAdapter(billRequestList, new BillRequestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BillRequest billRequest) {
                showBillDetailsDialog(billRequest);
            }

//            public void onApprovalAction(int customerId, int approvalId) {
//                updateApprovalStatus(customerId, approvalId);
//            }
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadBillRequests();
    }

    private void loadBillRequests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int customerId = prefs.getInt("user_id", -1);
        String authToken = prefs.getString("authToken", "");

        if (customerId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = APIs.GetBillRequestURL + "?customerId=" + customerId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResponse,
                error -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Error loading bill requests");
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (!authToken.isEmpty()) {
                    headers.put("Authorization", "Bearer " + authToken);
                }
                return headers;
            }
        };

        VolleyClient.getInstance(this).addToRequestQueue(request);
    }

    private void handleResponse(JSONArray response) {
        progressBar.setVisibility(View.GONE);

        try {
            billRequestList.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                BillRequest billRequest = new BillRequest();

                billRequest.setRequestID(obj.optInt("RequestID", 0));
                billRequest.setCustomersId(obj.optInt("CustomersId", 0));
                billRequest.setCustomerName(obj.optString("CustomerName", ""));
                billRequest.setCustomerEmail(obj.optString("CustomerEmail", ""));
                billRequest.setCustomerPhone(obj.optString("CustomerPhone", ""));
                billRequest.setDriverName(obj.optString("DriverName", ""));
                billRequest.setDriverPhone(obj.optString("DriverPhone", ""));
                billRequest.setApprovalId(obj.optInt("ApprovalId", 0));
                billRequest.setPickFrom(obj.optString("pickFrom", ""));
                billRequest.setDropAt(obj.optString("dropAt", ""));
                billRequest.setDistance(obj.optInt("distance", 0));
                billRequest.setFare(obj.optDouble("fare", 0.0));
                billRequest.setParkingCharges(obj.optDouble("Parking Charges", 0.0));
                billRequest.setWaitingCharges(obj.optDouble("Waiting Charges", 0.0));
                billRequest.setTollTax(obj.optDouble("Toll Tax", 0.0));
                billRequest.setDriverTip(obj.optDouble("Driver Tip", 0.0));
                billRequest.setBillDate(obj.optString("Bill Date", ""));
                billRequest.setTotal(obj.optDouble("Total", 0.0));
                billRequest.setActive(obj.optBoolean("IsActive", true));

                billRequestList.add(billRequest);
            }

            if (billRequestList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No bill requests found");
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(billRequestList);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateApprovalStatus(int customerId, int approvalId) {
        try {
            String url = UpdateBillApprovalURL;

            JSONObject requestBody = new JSONObject();
            requestBody.put("CustomersId", customerId);
            requestBody.put("ApprovalId", approvalId);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    response -> {
                        Toast.makeText(this, "Status updated successfully!", Toast.LENGTH_SHORT).show();
                        // Optionally reload data:
                        // loadBillRequests();
                    },
                    error -> {
                        Toast.makeText(this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );

            Volley.newRequestQueue(this).add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred while updating!", Toast.LENGTH_SHORT).show();
        }
    }



    private void showBillDetailsDialog(BillRequest billRequest) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bill_details, null);

        TextView tvRequestId = dialogView.findViewById(R.id.tvRequestId);
        TextView tvDriverName = dialogView.findViewById(R.id.tvDriverName);
        TextView tvDriverPhone = dialogView.findViewById(R.id.tvDriverPhone);
        TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
        TextView tvCustomerPhone = dialogView.findViewById(R.id.tvCustomerPhone);
        TextView tvPickFrom = dialogView.findViewById(R.id.tvPickFrom);
        TextView tvDropAt = dialogView.findViewById(R.id.tvDropAt);
        TextView tvDistance = dialogView.findViewById(R.id.tvDistance);
        TextView tvFare = dialogView.findViewById(R.id.tvFare);
        TextView tvParkingCharges = dialogView.findViewById(R.id.tvParkingCharges);
        TextView tvWaitingCharges = dialogView.findViewById(R.id.tvWaitingCharges);
        TextView tvTollTax = dialogView.findViewById(R.id.tvTollTax);
        TextView tvDriverTip = dialogView.findViewById(R.id.tvDriverTip);
        TextView tvBillDate = dialogView.findViewById(R.id.tvBillDate);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        android.widget.Button btnAccept = dialogView.findViewById(R.id.btnAccept);
        android.widget.Button btnReject = dialogView.findViewById(R.id.btnReject);

        tvRequestId.setText("Request #" + billRequest.getRequestID());
        tvDriverName.setText(billRequest.getDriverName());
        tvDriverPhone.setText(billRequest.getDriverPhone());
        tvCustomerName.setText(billRequest.getCustomerName());
        tvCustomerPhone.setText(billRequest.getCustomerPhone());
        tvPickFrom.setText(billRequest.getPickFrom());
        tvDropAt.setText(billRequest.getDropAt());
        tvDistance.setText(billRequest.getDistance() + " km");
        tvFare.setText(String.format("₹ %.2f", billRequest.getFare()));
        tvParkingCharges.setText(String.format("₹ %.2f", billRequest.getParkingCharges()));
        tvWaitingCharges.setText(String.format("₹ %.2f", billRequest.getWaitingCharges()));
        tvTollTax.setText(String.format("₹ %.2f", billRequest.getTollTax()));
        tvDriverTip.setText(String.format("₹ %.2f", billRequest.getDriverTip()));
        tvBillDate.setText(formatDate(billRequest.getBillDate()));
        tvTotal.setText(String.format("₹ %.2f", billRequest.getTotal()));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnAccept.setOnClickListener(v -> {
            updateApprovalStatus(billRequest.getCustomersId(), 1);
            dialog.dismiss();
        });

        btnReject.setOnClickListener(v -> {
            updateApprovalStatus(billRequest.getCustomersId(), 2);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void downloadBillPdf(int requestId) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String authToken = prefs.getString("authToken", "");

        String url = "https://tdm0f26m-7270.inc1.devtunnels.ms/api/CustomerBillRequest/GenerateCustomerBillPdf?billRequestId=" + requestId;

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            // Add authorization header
            if (!authToken.isEmpty()) {
                request.addRequestHeader("Authorization", "Bearer " + authToken);
            }

            request.setTitle("Bill #" + requestId);
            request.setDescription("Downloading bill PDF");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Bill_" + requestId + ".pdf");

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Downloading bill PDF...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }


    public interface OnApprovalActionListener {
        void onApprovalAction(int customerId, int approvalId);
    }


    public void setOnApprovalActionListener(OnApprovalActionListener listener) {
        this.approvalActionListener = listener;
    }

}