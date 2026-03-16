package com.sharpflux.taxiapp.data.model;

import com.google.gson.annotations.SerializedName;

public class OtpData {
    @SerializedName("requestId")
    private int requestId;

    @SerializedName("otp")
    private String otp;

    @SerializedName("driverId")
    private int driverId;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("distance")
    private double distance;
    public OtpData() {
        // Default constructor for Gson
    }

    public OtpData(int requestId, String otp, int driverId, String timestamp) {
        this.requestId = requestId;
        this.otp = otp;
        this.driverId = driverId;
        this.timestamp = timestamp;
        this.totalAmount = totalAmount;
        this.distance = distance;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    @Override
    public String toString() {
        return "OtpData{" +
                "requestId=" + requestId +
                ", otp='" + otp + '\'' +
                ", driverId=" + driverId +
                ", timestamp='" + timestamp + '\'' +
                ", totalAmount=" + totalAmount +
                ", distance=" + distance +
                '}';
    }
}