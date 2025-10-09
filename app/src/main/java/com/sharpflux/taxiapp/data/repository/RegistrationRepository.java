package com.sharpflux.taxiapp.data.repository;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sharpflux.taxiapp.data.model.DropdownItem;
import com.sharpflux.taxiapp.data.model.Registration;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.VolleyClient;
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import com.sharpflux.taxiapp.data.model.DropdownItem;


public class RegistrationRepository {
    private static final String TAG = "RegistrationRepository";
    private final Context context;
   // private static final String TAG = "RegistrationActivity";

   // private List<DropdownItem> stateList = new ArrayList<>();
  //  private List<DropdownItem> cityList = new ArrayList<>();
   // private ArrayAdapter<DropdownItem> stateAdapter;
  //  private ArrayAdapter<DropdownItem> cityAdapter;

    public RegistrationRepository(Context context) {
        this.context = context;
    }

    public void registerUser(Registration registration, RegistrationCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("firstName", registration.getFirstName());
            jsonBody.put("middleName", registration.getMiddleName());
            jsonBody.put("lastName", registration.getLastName());
            jsonBody.put("emailId", registration.getEmailId());
            jsonBody.put("phoneNumber", registration.getPhoneNumber());
            jsonBody.put("passwordHash", registration.getPasswordHash());
            jsonBody.put("address", registration.getAddress());
            jsonBody.put("cityId", registration.getCityId());
            jsonBody.put("stateId", registration.getStateId());
            jsonBody.put("roleId", registration.getRoleId());
            jsonBody.put("isActive", registration.isActive());
            // Add other fields as needed

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIs.RegisterURL,
                    jsonBody,
                    response -> {
                        Log.d(TAG, "Registration successful: " + response.toString());
                        callback.onSuccess(response);
                    },
                    error -> {
                        String errorMsg = "Registration failed";
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

        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage());
            callback.onError("Error: " + e.getMessage());
        }
    }
    public void getDropdownData(String searchTerm, int page, int pageSize, int type, int parentId,
                                DropdownCallback callback) {
        String url = "https://tdm0f26m-7270.inc1.devtunnels.ms/api/Utility/allDropdown?"
                + "page=" + page
                + "&pageSize=" + pageSize
                + "&type=" + type
                + "&parentId=" + parentId
                + "&searchTerm=" + Uri.encode(searchTerm);

        Log.d(TAG, "Requesting dropdown - URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<DropdownItem> items = new ArrayList<>();
                    try {
                        Log.d(TAG, "Raw response: " + response.toString());

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Log.d(TAG, "JSON keys: " + obj.keys().toString());

                            int id;
                            String text;

                            if (obj.has("Id")) {
                                id = obj.getInt("Id");
                            } else if (obj.has("id")) {
                                id = obj.getInt("id");
                            } else {
                                Log.e(TAG, "No Id/id key found in JSON: " + obj.toString());
                                callback.onResult(false, null, "Invalid response format: No Id field");
                                return;
                            }

                            if (obj.has("Texts")) {
                                text = obj.getString("Texts");
                            } else if (obj.has("texts")) {
                                text = obj.getString("texts");
                            } else if (obj.has("text")) {
                                text = obj.getString("text");
                            } else {
                                Log.e(TAG, "No Texts/texts/text key found in JSON: " + obj.toString());
                                callback.onResult(false, null, "Invalid response format: No Texts field");
                                return;
                            }

                            items.add(new DropdownItem(id, text));
                            Log.d(TAG, "Added item: ID=" + id + ", Text=" + text);
                        }

                        Log.d(TAG, "Successfully parsed " + items.size() + " items");
                        callback.onResult(true, items, "Success");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        callback.onResult(false, null, "Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    String msg = "Network error";
                    if (error.networkResponse != null) {
                        msg = "HTTP " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Error response body: " + responseBody);
                            msg += " - " + responseBody;
                        }
                    } else if (error.getMessage() != null) {
                        msg = error.getMessage();
                    }
                    Log.e(TAG, "Request failed: " + msg);
                    callback.onResult(false, null, msg);
                }
        );

        VolleyClient.getInstance(context).addToRequestQueue(request);
    }
    public interface RegistrationCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
    public interface DropdownCallback {
        void onResult(boolean success, List<DropdownItem> items, String message);
    }
}