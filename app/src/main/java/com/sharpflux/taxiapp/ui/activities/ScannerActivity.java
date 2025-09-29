package com.sharpflux.taxiapp.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.sharpflux.taxiapp.R;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private PreviewView previewView;
    private ImageButton btnBack, btnFlash, btnQR, btnMenu;
    private LinearLayout btnUploadFromGallery;
    private View centerOverlay;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isFlashEnabled = false;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        initViews();
        initBarcodeScanner();
        setupClickListeners();

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check camera permission and start camera
        checkPermissionsAndStartCamera();
    }

    private void checkPermissionsAndStartCamera() {
        if (allPermissionsGranted()) {
            Log.d(TAG, "Camera permissions granted, starting camera");
            startCamera();
        } else {
            Log.d(TAG, "Requesting camera permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        btnBack = findViewById(R.id.btnBack);
        btnFlash = findViewById(R.id.btnFlash);
        btnQR = findViewById(R.id.btnQR);
        btnMenu = findViewById(R.id.btnMenu);
        btnUploadFromGallery = findViewById(R.id.btnUploadFromGallery);
        centerOverlay = findViewById(R.id.centerOverlay);
    }

    private void initBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnFlash.setOnClickListener(v -> toggleFlash());

        btnQR.setOnClickListener(v ->
                Toast.makeText(this, "QR code info", Toast.LENGTH_SHORT).show());

        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show());

        btnUploadFromGallery.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Process image from gallery for QR code scanning
                processImageFromGallery(imageUri);
            }
        }
    }

    private void processImageFromGallery(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                onBarcodeDetected(rawValue);
                                break;
                            }
                        }
                        if (barcodes.isEmpty()) {
                            Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Gallery barcode scanning failed", e);
                        Toast.makeText(this, "Failed to scan image", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error processing gallery image", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Add a small delay to ensure the PreviewView is ready
                previewView.post(() -> bindCameraUseCases());

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider initialization failed", e);
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }

        // Preview use case
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
                .build();

        // Image analysis use case for barcode scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

        // Camera selector
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to lifecycle
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);

            // Set surface provider AFTER binding to lifecycle
            preview.setSurfaceProvider(ContextCompat.getMainExecutor(this),
                    previewView.getSurfaceProvider());

        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    @ExperimentalGetImage
    private void analyzeImage(ImageProxy imageProxy) {
        if (!isScanning) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null) {
                            onBarcodeDetected(rawValue);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Barcode scanning failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void onBarcodeDetected(String barcode) {
        runOnUiThread(() -> {
            isScanning = false;
            stopScanAnimation();

            // Show success feedback
            Toast.makeText(this, "QR Code Scanned Successfully!", Toast.LENGTH_LONG).show();

            // Process the scan result
            processScanResult(barcode);

            // Resume scanning after 3 seconds
            previewView.postDelayed(() -> {
                isScanning = true;
                startScanAnimation();
            }, 3000);
        });
    }

    private void processScanResult(String result) {
        Log.d(TAG, "Scan result: " + result);

        // Handle different types of QR codes
        if (result.startsWith("upi://") || result.contains("paytm") ||
                result.contains("googlepay") || result.contains("phonepe")) {
            // Handle payment QR codes
            handlePaymentQR(result);
        } else if (result.startsWith("http://") || result.startsWith("https://")) {
            // Handle URL QR codes
            handleURLQR(result);
        } else {
            // Handle other QR codes
            handleGenericQR(result);
        }
    }

    private void handlePaymentQR(String paymentData) {
        // TODO: Implement payment QR handling
        Toast.makeText(this, "Payment QR detected", Toast.LENGTH_SHORT).show();
    }

    private void handleURLQR(String url) {
        // TODO: Implement URL QR handling
        Toast.makeText(this, "URL QR detected: " + url, Toast.LENGTH_SHORT).show();
    }

    private void handleGenericQR(String data) {
        // TODO: Implement generic QR handling
        Toast.makeText(this, "QR Data: " + data, Toast.LENGTH_SHORT).show();
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashEnabled = !isFlashEnabled;
            camera.getCameraControl().enableTorch(isFlashEnabled);

            btnFlash.setImageResource(isFlashEnabled ?
                    R.drawable.ic_flash_on : R.drawable.ic_flash_off);

            Toast.makeText(this, isFlashEnabled ? "Flash ON" : "Flash OFF",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScanAnimation() {
        // Only start animation if camera is working
        previewView.post(() -> {
            if (centerOverlay != null) {
                centerOverlay.animate()
                        .alpha(0.1f)
                        .setDuration(1000)
                        .withEndAction(() -> {
                            if (isScanning) {
                                centerOverlay.animate()
                                        .alpha(0.3f)
                                        .setDuration(1000)
                                        .withEndAction(this::startScanAnimation);
                            }
                        });
            }
        });
    }

    private void stopScanAnimation() {
        if (centerOverlay != null) {
            centerOverlay.clearAnimation();
            centerOverlay.setAlpha(0.5f);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                Log.d(TAG, "Camera permission granted, starting camera");
                startCamera();
                startScanAnimation();
            } else {
                Log.e(TAG, "Camera permission denied");
                Toast.makeText(this, "Camera permission is required for scanning",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanning = true;
        startScanAnimation();

        // Restart camera if needed
        if (cameraProvider == null && allPermissionsGranted()) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isScanning = false;
        stopScanAnimation();
    }
}