package com.sharpflux.taxiapp.data.network;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyClient {
    private static VolleyClient instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleyClient(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyClient getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}