package com.sharpflux.taxiapp.ui.activities;
import static com.sharpflux.taxiapp.data.network.APIs.GetPayplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.model.PricingPlan;
import com.sharpflux.taxiapp.ui.adapter.PricingAdapter;

import org.json.JSONArray;

import java.util.List;

public class PricingPlansActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PricingAdapter pricingAdapter;
    private String userName;
    private String userPhone;
    private String userEmail;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricing_plans);

        // ✅ Get data from Intent
        Intent receivedIntent = getIntent();
        userName = receivedIntent.getStringExtra("userName");
        userPhone = receivedIntent.getStringExtra("userPhone");
        userEmail = receivedIntent.getStringExtra("userEmail");
        userId = receivedIntent.getIntExtra("userId", 0);

        // ✅ Fallback to SharedPreferences if Intent data is null
        if (userName == null || userName.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            userName = prefs.getString("user_name", "");
            userPhone = prefs.getString("user_phone", "");
            userEmail = prefs.getString("user_email", "");
            userId = prefs.getInt("user_id", 0);
            Log.d("PricingPlans", "Loaded from SharedPreferences");
        }

        Log.d("PricingPlans", "Received - Name: " + userName + ", Phone: " + userPhone);

        recyclerView = findViewById(R.id.recyclerPlans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchPlans();
    }

    private void fetchPlans() {

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                GetPayplan,
                null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("data");

                        List<PricingPlan> list = new Gson().fromJson(
                                arr.toString(),
                                new TypeToken<List<PricingPlan>>(){}.getType()
                        );

                        pricingAdapter = new PricingAdapter(PricingPlansActivity.this, list,userName, userPhone, userEmail,userId);
                        recyclerView.setAdapter(pricingAdapter);

                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "API Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}

