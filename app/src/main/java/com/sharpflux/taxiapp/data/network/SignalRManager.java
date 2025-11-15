package com.sharpflux.taxiapp.data.network;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.google.gson.Gson;
import com.sharpflux.taxiapp.data.model.OtpData;
import io.reactivex.rxjava3.core.Single;

public class SignalRManager {
    private static final String TAG = "SignalRManager";
    private static SignalRManager instance;
    private HubConnection hubConnection;
    private OtpListener otpListener;
    private Gson gson = new Gson();

    private SignalRManager() {}

    public static synchronized SignalRManager getInstance() {
        if (instance == null) {
            instance = new SignalRManager();
        }
        return instance;
    }

    public interface OtpListener {
        void onOtpReceived(int requestId, String otp, int driverId, String timestamp);
        void onConnectionStateChanged(boolean isConnected);
        void onError(Exception exception);
    }

    public void setOtpListener(OtpListener listener) {
        this.otpListener = listener;
    }

    public void connect(String serverUrl, int driverId) {
        try {
            if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                Log.d(TAG, "Already connected");
                return;
            }

            // Ensure serverUrl doesn't end with slash
            if (serverUrl.endsWith("/")) {
                serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
            }
            final String finalServerUrl = serverUrl;
            String hubUrl = serverUrl + "/OtpHub";
            Log.d("URL", "Connecting to: " + hubUrl);

            // Build hub connection with increased timeout
            hubConnection = HubConnectionBuilder.create(hubUrl)
                    .withHandshakeResponseTimeout(60000) // Increased to 60 seconds
                    .build();

            hubConnection.on("ReceiveOtp", (otpDataObj) -> {
                try {
                    String json = gson.toJson(otpDataObj);
                    Log.d(TAG, "Raw OTP JSON received: " + json);

                    OtpData otpData = gson.fromJson(json, OtpData.class);

                    if (otpListener != null) {
                        otpListener.onOtpReceived(
                                otpData.getRequestId(),
                                otpData.getOtp(),
                                otpData.getDriverId(),
                                otpData.getTimestamp()
                        );
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing OTP data", e);
                    if (otpListener != null) otpListener.onError(e);
                }
            }, Object.class);



            // Register driver registered confirmation
            hubConnection.on("DriverRegistered", (dId) -> {
                Log.d(TAG, "Driver registered with ID: " + dId);
            }, Integer.class);

            // Handle connection state changes
            hubConnection.onClosed((error) -> {
                Log.d(TAG, "Connection closed" + (error != null ? ": " + error.getMessage() : ""));
                if (otpListener != null) {
                    otpListener.onConnectionStateChanged(false);
                }
                // Auto-reconnect after 5 seconds
                reconnect(finalServerUrl, driverId, 5000);
            });

            // Start connection
            startConnection(driverId);
        } catch (Exception e) {
            Log.e(TAG, "Error in connect method", e);
            e.printStackTrace();
            if (otpListener != null) {
                otpListener.onError(e);
            }
        }
    }

    private void startConnection(int driverId) {
        hubConnection.start()
                .doOnComplete(() -> {
                    String connectionId = hubConnection.getConnectionId();
                    Log.d(TAG, "✅ Connected to SignalR hub. ConnectionId: " + connectionId);
                    Log.d(TAG, "✅ Connected to SignalR hub successfully");
                    if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                        // Register driver with the hub
                        hubConnection.invoke("RegisterDriver", driverId)
                                .doOnComplete(() -> Log.d(TAG, "✅ Driver " + driverId + " registered successfully"))
                                .doOnError(error -> {
                                    Log.e(TAG, "❌ Failed to register driver", error);
                                    error.printStackTrace();
                                })
                                .subscribe();

                        if (otpListener != null) {
                            otpListener.onConnectionStateChanged(true);
                        }
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "❌ Failed to connect to SignalR hub", error);
                    error.printStackTrace();
                    if (otpListener != null) {
                        otpListener.onError(new Exception(error));
                    }
                })
                .subscribe();
    }

    private void reconnect(String serverUrl, int driverId, long delayMillis) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (hubConnection == null || hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
                Log.d(TAG, "🔄 Attempting to reconnect...");
                connect(serverUrl, driverId);
            }
        }, delayMillis);
    }

    public void disconnect() {
        if (hubConnection != null) {
            hubConnection.stop();
            Log.d(TAG, "Disconnected from SignalR hub");
        }
    }

    public boolean isConnected() {
        return hubConnection != null &&
                hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    // Add this method for testing
    public HubConnectionState getConnectionState() {
        return hubConnection != null ? hubConnection.getConnectionState() : HubConnectionState.DISCONNECTED;
    }
}