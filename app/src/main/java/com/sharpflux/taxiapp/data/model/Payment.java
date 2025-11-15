package com.sharpflux.taxiapp.data.model;

public class Payment {

    // Basic Payment Information
    private int paymentId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // Driver Information
    private int driverId;
    private String fullName;
    private String email;
    private String phoneNumber;

    // Transaction Details
    private double amount;
    private String currency;
    private String status;

    // Constructor
    public Payment() {
        this.currency = "INR"; // default
        this.status = "Pending";
    }

    // Getters and Setters

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString() for debugging/logging
    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", razorpayOrderId='" + razorpayOrderId + '\'' +
                ", razorpayPaymentId='" + razorpayPaymentId + '\'' +
                ", razorpaySignature='" + razorpaySignature + '\'' +
                ", driverId=" + driverId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
