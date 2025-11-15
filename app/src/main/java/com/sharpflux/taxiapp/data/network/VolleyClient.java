package com.sharpflux.taxiapp.data.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class VolleyClient {

    private static volatile VolleyClient instance;
    private RequestQueue requestQueue;
    private final ImageLoader imageLoader;
    private final Context appContext;

    // Private constructor
    private VolleyClient(Context context) {
        this.appContext = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(appContext);

        this.imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(50);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleyClient getInstance(Context context) {
        if (instance == null) {
            synchronized (VolleyClient.class) {
                if (instance == null) {
                    instance = new VolleyClient(context);
                }
            }
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appContext);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null && tag != null) {
            requestQueue.cancelAll(tag);
        }
    }


    public ImageLoader getImageLoader() {
        return imageLoader;
    }
    public void clearCache() {
        if (requestQueue != null) {
            requestQueue.getCache().clear();
        }
    }
}
