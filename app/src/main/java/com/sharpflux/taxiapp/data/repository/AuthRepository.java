package com.sharpflux.taxiapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sharpflux.taxiapp.data.network.APIs;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthRepository {

    private Context context;

    public AuthRepository(Context context) {
        this.context = context;
    }

    public void login(String Email, String password, AuthCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject params = new JSONObject();
        try {
            params.put("Email", Email);
            params.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, APIs.LoginURL, params,
                response -> {
                    try {
                        // ✅ Extract data
                        String authToken = response.getString("authToken");
                        String refreshToken = response.optString("refreshToken", "");
                        String expiresIn = response.optString("expiresIn", "");
                        JSONObject user = response.getJSONObject("user");

                        // ✅ Save all data into SharedPreferences
                        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        // Token data
                        editor.putString("authToken", authToken);
                        editor.putString("refreshToken", refreshToken);
                        editor.putString("expiresIn", expiresIn);

                        // User data
                        editor.putInt("user_id", user.getInt("id"));
                        editor.putString("user_name", user.getString("name"));
                        editor.putString("user_email", user.getString("email"));
                        editor.putBoolean("isActive", user.getBoolean("isActive"));
                        editor.putInt("roleId", user.optInt("roleId", -1));
                        editor.putInt("locationId", user.optInt("locationId", -1));
                        editor.putString("roleName", user.optString("roleName", ""));
                        editor.putString("companyLogoURL", user.optString("companyLogoURL", ""));

                        editor.apply();

                        callback.onResult(true, "Login successful");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onResult(false, "Invalid server response");
                    }
                },
                error -> callback.onResult(false, "Network error: " + error.getMessage())
        );

        queue.add(request);
    }

//    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//
//    String authToken = prefs.getString("authToken", "");
//    String userName = prefs.getString("user_name", "");
//    String userEmail = prefs.getString("user_email", "");
//    String companyLogoURL = prefs.getString("companyLogoURL", "");
//    int roleId = prefs.getInt("roleId", -1);

    public interface AuthCallback {
        void onResult(boolean success, String message);
    }
}
