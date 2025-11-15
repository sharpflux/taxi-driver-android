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


    public OtpData() {
        // Default constructor for Gson
    }

    public OtpData(int requestId, String otp, int driverId, String timestamp) {
        this.requestId = requestId;
        this.otp = otp;
        this.driverId = driverId;
        this.timestamp = timestamp;
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

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OtpData{" +
                "requestId=" + requestId +
                ", otp='" + otp + '\'' +
                ", driverId=" + driverId +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}