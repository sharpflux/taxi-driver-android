package com.sharpflux.taxiapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.sharpflux.taxiapp.data.model.Customer;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.VolleyClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private static final String TAG = "CustomerRepository";
    private final Context context;

    public CustomerRepository(Context context) {
        this.context = context;
    }

    public void getCustomers(int startIndex, int pageSize, String searchBy, String searchCriteria,int customersId, CustomerCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
         customersId = prefs.getInt("user_id", 0);
        String url = APIs.GetCustomersURL +
                "?startIndex=" + startIndex +
                "&pageSize=" + pageSize +
                "&searchBy=" + searchBy +
                "&searchCriteria=" + searchCriteria +
                "&CustomersId=" + customersId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Customer> customers = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Customer customer = parseCustomer(obj);
                            customers.add(customer);
                        }
                        Log.d(TAG, "Customers fetched successfully: " + customers.size());
                        callback.onSuccess(customers);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        callback.onError("Error parsing data: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Failed to fetch customers";
                    if (error.networkResponse != null) {
                        errorMsg += ": " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            errorMsg += " - " + new String(error.networkResponse.data);
                        }
                    } else if (error.getMessage() != null) {
                        errorMsg += ": " + error.getMessage();
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
        );

        VolleyClient.getInstance(context).addToRequestQueue(request);
    }

    public void getCustomerById(int customerId, CustomerCallback callback) {
        getCustomers(1, 1, "0", "0",customerId, new CustomerCallback() {
            @Override
            public void onSuccess(List<Customer> customers) {
                if (customers != null && !customers.isEmpty()) {
                    List<Customer> singleCustomer = new ArrayList<>();
                    singleCustomer.add(customers.get(0));
                    callback.onSuccess(singleCustomer);
                } else {
                    callback.onError("Customer not found");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private Customer parseCustomer(JSONObject obj) throws Exception {
        Customer customer = new Customer();

        customer.setCustomersId(obj.optInt("CustomersId", 0));

        // Parse FullName and split into first, middle, last
        String fullName = obj.optString("FullName", "");
        String[] nameParts = fullName.split(" ");
        if (nameParts.length >= 1) {
            customer.setFirstName(nameParts[0]);
        }
        if (nameParts.length >= 3) {
            customer.setMiddleName(nameParts[1]);
            customer.setLastName(nameParts[2]);
        } else if (nameParts.length == 2) {
            customer.setLastName(nameParts[1]);
        }

        customer.setEmailId(getStringValue(obj, "EmailId"));
        customer.setPhoneNumber(obj.optString("PhoneNumber", ""));
        customer.setAddress(getStringValue(obj, "Address"));
        customer.setCityId(getIntValue(obj, "CityId"));
        customer.setStateId(getIntValue(obj, "StateId"));
        customer.setRoleId(getIntValue(obj, "RoleId"));
        customer.setIsActive(obj.optBoolean("IsActive", true));
        customer.setLocationId(obj.optInt("LocationId", 0));

        return customer;
    }

    private String getStringValue(JSONObject obj, String key) {
        try {
            Object value = obj.opt(key);
            if (value == null || value == JSONObject.NULL) {
                return "";
            }
            if (value instanceof JSONObject) {
                return "";
            }
            return String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }

    private int getIntValue(JSONObject obj, String key) {
        try {
            Object value = obj.opt(key);
            if (value == null || value == JSONObject.NULL) {
                return 0;
            }
            if (value instanceof JSONObject) {
                return 0;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    public interface CustomerCallback {
        void onSuccess(List<Customer> customers);
        void onError(String error);
    }
}