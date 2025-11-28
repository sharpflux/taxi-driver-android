package com.sharpflux.taxiapp.ui.activities;
import static com.sharpflux.taxiapp.data.network.APIs.GetPayplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.PricingPlan;
import com.sharpflux.taxiapp.ui.adapter.PricingAdapter;

import org.json.JSONArray;

import java.util.List;

public class PricingPlansActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PricingAdapter pricingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricing_plans);

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

                        pricingAdapter = new PricingAdapter(PricingPlansActivity.this, list);
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

