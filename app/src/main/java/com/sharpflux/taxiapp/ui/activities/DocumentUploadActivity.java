package com.sharpflux.taxiapp.ui.activities;

import static com.sharpflux.taxiapp.data.network.APIs.GetDocumentURL;
import static com.sharpflux.taxiapp.data.network.APIs.GetDriverDocument;
import static com.sharpflux.taxiapp.data.network.APIs.UPDATE_DOCUMENT_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.sharpflux.logomobility.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class DocumentUploadActivity extends AppCompatActivity {

    private LinearLayout docContainer;
    private int selectedDocTypeId = 0;
    private String driverId;

    // Store preview ImageView & Placeholder per DocumentTypeId
    private final HashMap<Integer, ImageView> docImageMap = new HashMap<>();
    private final HashMap<Integer, LinearLayout> placeholderMap = new HashMap<>();
    private final HashMap<Integer, MaterialButton> buttonMap = new HashMap<>();
    private final HashMap<Integer, Integer> driverDetailIdMap = new HashMap<>();


    // Store DocumentTypeIds that have VerificationStatusId = 1
    private final HashSet<Integer> editableDocuments = new HashSet<>();

    // Store selected image URIs for each document
    private final HashMap<Integer, Uri> selectedImages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_upload);

        //notification bar
        Window window = getWindow();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            window.getDecorView().setSystemUiVisibility(0); // remove light icons flag
        } else {
            // Light mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        docContainer = findViewById(R.id.docContainer);

        // Get driverId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driversId = prefs.getInt("user_id", 0);

        if (driversId == 0) {
            Toast.makeText(this, "Driver ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Convert to String for API call
        driverId = String.valueOf(driversId);

        // First check driver documents to get verification status
        checkDriverDocuments();
    }

    private void checkDriverDocuments() {

        // Always normalize URL
        String base = GetDriverDocument;
        if (!base.endsWith("/")) {
            base = base + "/";
        }

        String url = base + driverId;

        System.out.println("📌 Driver Document URL = " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::processDriverDocuments,
                error -> {

                    String msg = "Failed to load document status";

                    if (error.networkResponse != null) {
                        msg += " (HTTP " + error.networkResponse.statusCode + ")";
                        String responseTxt = new String(error.networkResponse.data);
                        System.err.println(" API Error Response: " + responseTxt);
                    }

                    error.printStackTrace();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                    // Continue loading documents
                    fetchDocumentsFromAPI();
                }
        );

        // MUST ADD THIS — prevents intermittent failures
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                10000,   // 10 sec
                2,       // Retry 2 times
                1f
        ));

        Volley.newRequestQueue(this).add(request);
    }


    private void processDriverDocuments(JSONArray array) {
        editableDocuments.clear();
        driverDetailIdMap.clear();

        boolean hasIssue = false;

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;

                int documentTypeId = obj.optInt("DocumentTypeId", 0);
                int verificationStatusId = obj.optInt("VerificationStatusId", 0);
                int driverDetailId = obj.optInt("DriverDetailId", 0);

                driverDetailIdMap.put(documentTypeId, driverDetailId);

                if (verificationStatusId == 2) {
                    editableDocuments.add(documentTypeId);
                    hasIssue = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvIssueMessage = findViewById(R.id.tvIssueMessage);
        tvIssueMessage.setVisibility(hasIssue ? View.VISIBLE : View.GONE);

        if (editableDocuments.isEmpty()) {
            showNoPendingDocuments();
            return;
        }

        fetchDocumentsFromAPI();
    }


    private void showNoPendingDocuments() {
        docContainer.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText("No pending documents to upload");
        tv.setTextSize(18);
        tv.setPadding(40, 80, 40, 80);

        docContainer.addView(tv);
    }


    private void fetchDocumentsFromAPI() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                GetDocumentURL,
                null,
                this::loadDocuments,
                Throwable::printStackTrace
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadDocuments(JSONArray array) {
        docContainer.removeAllViews();

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String docName = obj.getString("DocumentName");
                int docTypeId = obj.getInt("DocumentTypeId");
                String docUrl = obj.optString("DocumentURL", "");

                // Only show documents that are editable (VerificationStatusId = 1)
                if (editableDocuments.contains(docTypeId)) {
                    addDocument(docName, docTypeId, docUrl);
                }
            }

            // Add a submit button at the bottom
            addSubmitButton();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDocument(String documentName, int docTypeId, String documentUrl) {
        View view = getLayoutInflater().inflate(R.layout.item_document_upload, null);

        TextView tvTitle = view.findViewById(R.id.tvDocumentName);
        LinearLayout llPlaceholder = view.findViewById(R.id.llPlaceholder);
        ImageView ivDocument = view.findViewById(R.id.ivDocument);
        MaterialButton btnUpload = view.findViewById(R.id.btnUploadDocument);

        tvTitle.setText(documentName);

        // Default UI state
        ivDocument.setVisibility(View.GONE);
        llPlaceholder.setVisibility(View.VISIBLE);

        // Store references in maps
        docImageMap.put(docTypeId, ivDocument);
        placeholderMap.put(docTypeId, llPlaceholder);
        buttonMap.put(docTypeId, btnUpload);

        // Enable upload button (all shown documents are editable)
        btnUpload.setEnabled(true);
        btnUpload.setOnClickListener(v -> {
            selectedDocTypeId = docTypeId;
            chooseImage();
        });

        docContainer.addView(view);
    }

    private void addSubmitButton() {
        MaterialButton btnSubmit = new MaterialButton(this);
        btnSubmit.setText("Submit Documents");
        btnSubmit.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnSubmit.getLayoutParams();
        params.setMargins(32, 32, 32, 32);
        btnSubmit.setLayoutParams(params);

        btnSubmit.setOnClickListener(v -> submitDocuments());

        docContainer.addView(btnSubmit);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null && selectedDocTypeId != 0) {
                // Double check if document is editable
                if (!editableDocuments.contains(selectedDocTypeId)) {
                    Toast.makeText(this, "Document cannot be edited", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Store the selected image URI
                selectedImages.put(selectedDocTypeId, imageUri);

                ImageView img = docImageMap.get(selectedDocTypeId);
                LinearLayout placeholder = placeholderMap.get(selectedDocTypeId);

                if (img != null) {
                    img.setImageURI(imageUri);
                    img.setVisibility(View.VISIBLE);
                }
                if (placeholder != null) {
                    placeholder.setVisibility(View.GONE);
                }
            }
        }
    }

    private void submitDocuments() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please select at least one document", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Build the request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("driverId", Integer.parseInt(driverId));
            requestBody.put("updatedBy", Integer.parseInt(driverId)); // You can change this if needed

            JSONArray documentsArray = new JSONArray();

            // Process each selected image
            for (Integer docTypeId : selectedImages.keySet()) {
                Uri imageUri = selectedImages.get(docTypeId);
                if (imageUri != null) {
                    JSONObject docObj = new JSONObject();
                    int driverDetailId = driverDetailIdMap.getOrDefault(docTypeId, 0);
                    docObj.put("driverDetailId", driverDetailId);
                    docObj.put("documentTypeId", docTypeId);

                    // Convert image to base64
                    String base64Image = convertImageToBase64(imageUri);
                    docObj.put("documentImage", "data:image/jpeg;base64," + base64Image);

                    // Set document validity (you can add date picker for this)
                    docObj.put("documentValidTo", "2026-12-31T23:59:59.000Z");

                    docObj.put("verificationStatusId", 1); // Pending verification
                    docObj.put("rejectionReason", "");

                    documentsArray.put(docObj);
                }
            }

            requestBody.put("documents", documentsArray);

            // Make the API call
            uploadDocuments(requestBody);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing documents", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Compress image if needed
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void uploadDocuments(JSONObject requestBody) {
        Toast.makeText(this, "Uploading documents...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                UPDATE_DOCUMENT_URL,
                requestBody,
                response -> {
                    Toast.makeText(this, "Driver documents updated successfully!", Toast.LENGTH_LONG).show();

                    // Clear selected images
                    selectedImages.clear();

                    // Redirect to VerificationCheckActivity
                    Intent intent = new Intent(DocumentUploadActivity.this, VerificationCheckActivity.class);
                    startActivity(intent);

                    // Optionally refresh or close the screen
                    finish();
                },

                error -> {
                    error.printStackTrace();
                    String errorMsg = "Upload failed";
                    if (error.networkResponse != null) {
                        errorMsg += ": " + new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add authorization header if needed
                // headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}