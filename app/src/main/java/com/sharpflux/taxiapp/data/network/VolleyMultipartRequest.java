package com.sharpflux.taxiapp.data.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return buildMultipartBody();
    }

    protected Map<String, String> getParams() {
        return null;
    }

    protected Map<String, DataPart> getByteData() {
        return null;
    }

    private byte[] buildMultipartBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        String boundary = "apiclient-" + System.currentTimeMillis();
        String twoHyphens = "--";
        String lineEnd = "\r\n";

        try {

            // Text params
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                for (String key : params.keySet()) {
                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd).getBytes());
                    bos.write(lineEnd.getBytes());
                    bos.write(params.get(key).getBytes());
                    bos.write(lineEnd.getBytes());
                }
            }

            // File params
            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                for (String key : data.keySet()) {
                    DataPart part = data.get(key);

                    bos.write((twoHyphens + boundary + lineEnd).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + key +
                            "\"; filename=\"" + part.getFileName() + "\"" + lineEnd).getBytes());
                    bos.write(("Content-Type: image/jpeg" + lineEnd).getBytes());
                    bos.write(lineEnd.getBytes());

                    bos.write(part.getContent());

                    bos.write(lineEnd.getBytes());
                }
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;

        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
