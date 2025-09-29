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
                        // Parse user and token
                        JSONObject user = response.getJSONObject("user");
                        String token = response.getString("token");

                        // Save token in SharedPreferences (optional)
                        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("auth_token", token).apply();

                        // You can also save user info if needed
                        prefs.edit().putString("user_name", user.getString("name")).apply();
                        prefs.edit().putString("user_email", user.getString("email")).apply();

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

    public interface AuthCallback {
        void onResult(boolean success, String message);
    }
}
