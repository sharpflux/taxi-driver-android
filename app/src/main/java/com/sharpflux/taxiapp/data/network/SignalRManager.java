package com.sharpflux.taxiapp.data.network;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.google.gson.Gson;
import com.sharpflux.taxiapp.data.model.OtpData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignalRManager {
    private static final String TAG = "SignalRManager";
    private static SignalRManager instance;
    private HubConnection hubConnection;
    private final List<OtpListener> otpListeners = new CopyOnWriteArrayList<>();
    private Gson gson = new Gson();

    private SignalRManager() {}

    public static synchronized SignalRManager getInstance() {
        if (instance == null) {
            instance = new SignalRManager();
        }
        return instance;
    }

    public interface OtpListener {
        void onOtpReceived(int requestId, String otp, int driverId,
                           String timestamp, double totalAmount, double distance);
        void onConnectionStateChanged(boolean isConnected);
        void onError(Exception exception);
    }

    public void addOtpListener(OtpListener listener) {
        if (listener != null && !otpListeners.contains(listener)) {
            otpListeners.add(listener);
            Log.d(TAG, "Listener added. Total listeners: " + otpListeners.size());
        }
    }

    public void removeOtpListener(OtpListener listener) {
        if (listener != null) {
            otpListeners.remove(listener);
            Log.d(TAG, "Listener removed. Total listeners: " + otpListeners.size());
        }
    }

    @Deprecated
    public void setOtpListener(OtpListener listener) {
        otpListeners.clear();
        if (listener != null) {
            otpListeners.add(listener);
        }
    }

    private void notifyOtpReceived(int requestId, String otp, int driverId,
                                   String timestamp, double totalAmount, double distance) {
        for (OtpListener listener : otpListeners) {
            listener.onOtpReceived(requestId, otp, driverId, timestamp, totalAmount, distance);
        }
    }

    private void notifyConnectionStateChanged(boolean isConnected) {
        for (OtpListener listener : otpListeners) {
            listener.onConnectionStateChanged(isConnected);
        }
    }

    private void notifyError(Exception e) {
        for (OtpListener listener : otpListeners) {
            listener.onError(e);
        }
    }

    public void connect(String serverUrl, int driverId) {
        try {
            if (hubConnection != null &&
                    hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                Log.d(TAG, "Already connected");
                return;
            }

            if (serverUrl.endsWith("/")) {
                serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
            }
            final String finalServerUrl = serverUrl;
            String hubUrl = serverUrl + "/OtpHub";
            Log.d(TAG, "Connecting to: " + hubUrl);

            hubConnection = HubConnectionBuilder.create(hubUrl)
                    .withHandshakeResponseTimeout(60000)
                    .build();

            hubConnection.on("ReceiveOtp", (otpDataObj) -> {
                try {
                    String json = gson.toJson(otpDataObj);
                    Log.d(TAG, "Raw OTP JSON received: " + json);

                    OtpData otpData = gson.fromJson(json, OtpData.class);

                    notifyOtpReceived(
                            otpData.getRequestId(),
                            otpData.getOtp(),
                            otpData.getDriverId(),
                            otpData.getTimestamp(),
                            otpData.getTotalAmount(),
                            otpData.getDistance()
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing OTP data", e);
                    notifyError(e);
                }
            }, Object.class);

            hubConnection.on("DriverRegistered", (dId) -> {
                Log.d(TAG, "Driver registered with ID: " + dId);
            }, Integer.class);

            hubConnection.onClosed((error) -> {
                Log.d(TAG, "Connection closed" +
                        (error != null ? ": " + error.getMessage() : ""));
                notifyConnectionStateChanged(false);
                reconnect(finalServerUrl, driverId, 5000);
            });

            startConnection(driverId);
        } catch (Exception e) {
            Log.e(TAG, "Error in connect method", e);
            notifyError(e);
        }
    }

    private void startConnection(int driverId) {
        hubConnection.start()
                .doOnComplete(() -> {
                    Log.d(TAG, "✅ Connected. ConnectionId: " + hubConnection.getConnectionId());

                    if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                        hubConnection.invoke("RegisterDriver", driverId)
                                .doOnComplete(() ->
                                        Log.d(TAG, "✅ Driver " + driverId + " registered"))
                                .doOnError(error ->
                                        Log.e(TAG, "❌ Failed to register driver", error))
                                .subscribe();

                        notifyConnectionStateChanged(true);
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "❌ Failed to connect", error);
                    notifyError(new Exception(error));
                })
                .subscribe();
    }

    private void reconnect(String serverUrl, int driverId, long delayMillis) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (hubConnection == null ||
                    hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
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

    public HubConnectionState getConnectionState() {
        return hubConnection != null ?
                hubConnection.getConnectionState() : HubConnectionState.DISCONNECTED;
    }
}