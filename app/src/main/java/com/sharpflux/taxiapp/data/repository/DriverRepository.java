package com.sharpflux.taxiapp.data.repository;

import static com.sharpflux.taxiapp.data.network.APIs.DriverRegisterURL;
import static com.sharpflux.taxiapp.data.network.APIs.GetDocumentURL;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sharpflux.taxiapp.data.model.DocumentType;
import com.sharpflux.taxiapp.data.model.Driver;
import com.sharpflux.taxiapp.data.model.DropdownItem;
import com.sharpflux.taxiapp.data.network.APIs;
import com.sharpflux.taxiapp.data.network.VolleyClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DriverRepository {

    private static final String TAG = "DriverRepository";
    private final Context context;

    public DriverRepository(Context context) {
        this.context = context;
    }

    // DROPDOWN DATA FETCH
    public void getDropdownData(String searchTerm, int page, int pageSize, int type, int parentId,
                                DropdownCallback callback) {

        String url = "https://6kt492jn-7270.inc1.devtunnels.ms/api/Utility/allDropdown?" +
                "page=" + page +
                "&pageSize=" + pageSize +
                "&type=" + type +
                "&parentId=" + parentId +
                "&searchTerm=" + Uri.encode(searchTerm);

        Log.d(TAG, "Requesting dropdown - URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<DropdownItem> items = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int id = obj.has("Id") ? obj.getInt("Id") : obj.getInt("id");
                            String text = obj.has("Texts") ? obj.getString("Texts") :
                                    obj.has("texts") ? obj.getString("texts") :
                                            obj.getString("text");

                            items.add(new DropdownItem(id, text));
                        }
                        callback.onResult(true, items, "Success");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        callback.onResult(false, null, "Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    String msg = parseVolleyError(error);
                    Log.e(TAG, "Dropdown request failed: " + msg);
                    callback.onResult(false, null, msg);
                }
        );
        VolleyClient.getInstance(context).addToRequestQueue(request);
    }

    // GET DOCUMENT TYPES
    public void getDocumentTypes(DocumentTypeCallback callback) {
        Log.d(TAG, "Fetching document types from: " + GetDocumentURL);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                GetDocumentURL,
                null,
                response -> {
                    List<DocumentType> documentTypes = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            DocumentType docType = new DocumentType();

                            docType.setDocumentTypeId(obj.optInt("DocumentTypeId", 0));
                            docType.setDocumentName(obj.optString("DocumentName", ""));
                            docType.setActive(obj.optBoolean("IsActive", true));

                            documentTypes.add(docType);
                        }
                        callback.onResult(true, documentTypes, "Success");
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error for document types", e);
                        callback.onResult(false, null, "Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    String msg = parseVolleyError(error);
                    callback.onResult(false, null, msg);
                }
        );

        VolleyClient.getInstance(context).addToRequestQueue(request);
    }

    // ✅ FIXED: Register driver with correct request body structure
    public void registerDriver(Driver data, RegistrationCallback callback) {
        Log.d(TAG, "=== DRIVER REGISTRATION START ===");
        Log.d(TAG, "URL: " + DriverRegisterURL);

        try {
            JSONObject params = new JSONObject();

            // Basic Info
            params.put("driversId", 0);
            params.put("driverCode", data.getDriverCode() != null ? data.getDriverCode() : "DRV" + System.currentTimeMillis());
            params.put("firstName", data.getFirstName());
            params.put("middleName", data.getMiddleName() != null ? data.getMiddleName() : "");
            params.put("lastName", data.getLastName());
            params.put("emailId", data.getEmailId());
            params.put("phoneNumber", data.getPhoneNumber());
            params.put("passwordHash", data.getPassword());
            params.put("profileImage", JSONObject.NULL);

            // Address Info
            params.put("address", data.getAddress() != null ? data.getAddress() : "");
            params.put("cityId", data.getCityId());
            params.put("stateId", data.getStateId());
            params.put("locationId", data.getLocationId());
            params.put("genderId", data.getGenderId());
            params.put("termsConditions", data.isTermsConditions());
            params.put("aadharNumber", data.getAadharNumber());

            // Vehicle Info
            params.put("vehicleTypeId", data.getVehicleTypeId());
            params.put("vehicleNumber", data.getVehicleNumber() != null ? data.getVehicleNumber() : "");

            // Meta & Flags
            params.put("roleId", data.getRoleId());
            params.put("isVerified", data.isVerified());
            params.put("statusId", 1);
            params.put("verificationDate", convertToISODateTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date())));
            params.put("verifiedBy", data.getVerifiedBy());
            params.put("createdBy", 0);

            // ✅ DETAILS ARRAY - Dynamic Documents
            JSONArray detailsArray = new JSONArray();
            Map<Integer, String> base64Map = data.getDocumentBase64Map();

            if (base64Map != null && !base64Map.isEmpty()) {
                Log.d(TAG, "Processing " + base64Map.size() + " documents");

                for (Map.Entry<Integer, String> entry : base64Map.entrySet()) {
                    String base64Data = entry.getValue();

                    // Validate Base64
                    if (base64Data == null || base64Data.trim().isEmpty()) {
                        Log.e(TAG, "❌ Skipping document " + entry.getKey() + " - empty Base64");
                        callback.onResult(false, "Document upload failed for ID: " + entry.getKey());
                        return;
                    }

                    base64Data = base64Data.replaceAll("\\s+", "");

                    if (!base64Data.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                        Log.e(TAG, "❌ Document " + entry.getKey() + " has invalid Base64 format");
                        callback.onResult(false, "Invalid document format for ID: " + entry.getKey());
                        return;
                    }

                    JSONObject detailObj = new JSONObject();
                    detailObj.put("driverDetailId", 0);
                    detailObj.put("documentTypeId", entry.getKey());
                    detailObj.put("documentImage", base64Data);
                    detailObj.put("documentValidTo", convertToISODateTime(data.getDlValidTo() != null ? data.getDlValidTo() : "2030-12-31"));
                    detailObj.put("verificationStatusId", 0);
                    detailObj.put("rejectionReason", JSONObject.NULL);

                    detailsArray.put(detailObj);

                    Log.d(TAG, "✅ Document " + entry.getKey() + " - Size: " + (base64Data.length() / 1024) + " KB");
                }
            } else {
                Log.e(TAG, "❌ No documents found!");
                callback.onResult(false, "No documents uploaded");
                return;
            }

            params.put("details", detailsArray);

            // ✅ LANGUAGES ARRAY
            JSONArray languagesArray = new JSONArray();
            JSONObject languageObj = new JSONObject();
            languageObj.put("driverLanguageId", 0);
            languageObj.put("driversId", 0);
            languageObj.put("languageId", data.getLanguageId());
            languageObj.put("canRead", data.isUnderstand());
            languageObj.put("canWrite", false);
            languageObj.put("canSpeak", data.isSpeak());
            languagesArray.put(languageObj);

            params.put("languages", languagesArray);

            // Log payload size
            int payloadSize = params.toString().length();
            Log.d(TAG, "Payload Size: " + (payloadSize / 1024) + " KB");
            Log.d(TAG, "Total documents: " + detailsArray.length());
            Log.d(TAG, "Language ID: " + data.getLanguageId());

            sendDriverRequest(params, callback);

        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON creation error", e);
            callback.onResult(false, "Invalid JSON: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error", e);
            callback.onResult(false, "Error: " + e.getMessage());
        }
    }

    // Send request
    private void sendDriverRequest(JSONObject params, RegistrationCallback callback) {
        CustomJsonRequest request = new CustomJsonRequest(
                Request.Method.POST,
                DriverRegisterURL,
                params.toString(),
                response -> {
                    Log.d(TAG, "=== SUCCESS ===");
                    Log.d(TAG, "Response: " + response.toString());

                    try {
                        boolean success = response.optBoolean("success", true);
                        String message = response.optString("message", "Driver registered successfully!");

                        if (response.has("driversId")) {
                            int driverId = response.optInt("driversId");
                            message = "Registration successful! Driver ID: " + driverId;
                        } else if (response.has("data")) {
                            message = "Registration successful! ID: " + response.optString("data");
                        }

                        callback.onResult(success, message);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing success response", e);
                        callback.onResult(true, "Registration completed");
                    }
                },
                error -> {
                    Log.e(TAG, "=== REGISTRATION ERROR ===");
                    String errorMsg = parseVolleyError(error);
                    Log.e(TAG, "Final error: " + errorMsg);
                    callback.onResult(false, errorMsg);
                }
        );

        // 2 minute timeout for large payloads
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                120000,
                0,
                1.0f
        ));

        VolleyClient.getInstance(context).addToRequestQueue(request);
    }

    // CUSTOM JSON REQUEST
    private static class CustomJsonRequest extends Request<JSONObject> {
        private final String requestBody;
        private final Response.Listener<JSONObject> listener;

        public CustomJsonRequest(int method, String url, String requestBody,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.requestBody = requestBody;
            this.listener = listener;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            headers.put("Accept", "application/json");
            return headers;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            try {
                return requestBody.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Encoding error", e);
                return null;
            }
        }

        @Override
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                JSONObject result = new JSONObject(jsonString);
                return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new VolleyError(e));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            listener.onResponse(response);
        }
    }

    // UTILITY: Parse Volley errors
    private String parseVolleyError(VolleyError error) {
        String errorMsg = "Request failed";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMsg = "HTTP " + statusCode;

            if (error.networkResponse.data != null) {
                String body = new String(error.networkResponse.data);
                Log.e(TAG, "Error response: " + body);

                try {
                    JSONObject errorJson = new JSONObject(body);
                    if (errorJson.has("message")) {
                        errorMsg = errorJson.getString("message");
                    } else if (errorJson.has("error")) {
                        errorMsg = errorJson.getString("error");
                    } else if (errorJson.has("title")) {
                        errorMsg = errorJson.getString("title");
                    } else {
                        errorMsg = body;
                    }
                } catch (JSONException e) {
                    errorMsg = "HTTP " + statusCode + ": " + body;
                }
            }
        } else if (error.getMessage() != null) {
            errorMsg = error.getMessage();
        }

        return errorMsg;
    }

    // UTILITY: Convert date to ISO format
    private String convertToISODateTime(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateStr);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Date conversion error: " + dateStr, e);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return outputFormat.format(new Date());
        }
    }

    // GET DRIVER BY ID
    public void getDrivers(int driversId, DriverCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        driversId = prefs.getInt("user_id", 0);

        if (driversId <= 0) {
            callback.onError("Invalid driver ID");
            return;
        }

        String url = APIs.GetDriversURL
                + "?startIndex=1"
                + "&pageSize=10"
                + "&searchBy=0"
                + "&searchCriteria=0"
                + "&DriverId=" + driversId;

        Log.d(TAG, "Fetching driver: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Driver> drivers = new ArrayList<>();
                        if (response.length() > 0) {
                            JSONObject obj = response.getJSONObject(0);
                            drivers.add(parseDriver(obj));
                        }
                        callback.onSuccess(drivers);
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                error -> callback.onError(parseVolleyError(error))
        );

        VolleyClient.getInstance(context).addToRequestQueue(request);
    }


    public void getDriverById(int driverId, DriverCallback callback) {
        getDrivers(driverId, callback);
    }

    private Driver parseDriver(JSONObject obj) throws Exception {
        Log.d(TAG, "=== PARSING DRIVER START ===");
        Log.d(TAG, "JSON Object: " + obj.toString());

        Driver driver = new Driver();

        // Parse with EXACT field names from API (Capital case)
        driver.setDriverId(obj.optInt("DriverId", 0));
        Log.d(TAG, "DriverId: " + driver.getDriverId());

        driver.setDriverCode(obj.optString("DriverCode", ""));
        Log.d(TAG, "DriverCode: " + driver.getDriverCode());

        driver.setFirstName(obj.optString("FirstName", ""));
        Log.d(TAG, "FirstName: " + driver.getFirstName());

        driver.setMiddleName(obj.optString("MiddleName", ""));
        driver.setLastName(obj.optString("LastName", ""));
        Log.d(TAG, "LastName: " + driver.getLastName());

        driver.setEmailId(obj.optString("EmailId", ""));
        Log.d(TAG, "EmailId: " + driver.getEmailId());

        driver.setPhoneNumber(obj.optString("PhoneNumber", ""));
        Log.d(TAG, "PhoneNumber: " + driver.getPhoneNumber());

        driver.setAddress(obj.optString("Address", ""));

        driver.setCityId(obj.optInt("CityId", 0));
        Log.d(TAG, "CityId: " + driver.getCityId());

        driver.setStateId(obj.optInt("StateId", 0));
        Log.d(TAG, "StateId: " + driver.getStateId());

        driver.setAddress(obj.optString("Address",""));
        driver.setGenderId(obj.optInt("GenderId", 0));
        driver.setLocationId(obj.optInt("LocationId", 0));
        driver.setIsActive(obj.optBoolean("IsActive", true));
        driver.setAadharNumber(obj.optString("AadharNumber", ""));
        driver.setInsuranceValidTo(obj.optString("InsuranceValidTo", null));
        driver.setVehicleTypeId(obj.optInt("VehicleTypeId", 0));
        driver.setVehicleNumber(obj.optString("VehicleNumber", ""));
        driver.setLanguageId(obj.optInt("LanguageId", 0));
        driver.setRejectionReason(obj.optString("RejectionReason", ""));
        driver.setVerified(obj.optBoolean("IsVerified", false));
        driver.setStatusId(obj.optInt("StatusId", 0));
        driver.setVerifiedBy(obj.optInt("VerifiedBy", 0));
        driver.setRoleId(obj.optInt("RoleId", 0));

        Log.d(TAG, "=== PARSING DRIVER END ===");
        return driver;
    }

    // Callback Interfaces
    public interface DriverCallback {
        void onSuccess(List<Driver> drivers);
        void onError(String error);
    }

    public interface DropdownCallback {
        void onResult(boolean success, List<DropdownItem> items, String message);
    }

    public interface RegistrationCallback {
        void onResult(boolean success, String message);
    }

    public interface DocumentTypeCallback {
        void onResult(boolean success, List<DocumentType> documentTypes, String message);
    }
}