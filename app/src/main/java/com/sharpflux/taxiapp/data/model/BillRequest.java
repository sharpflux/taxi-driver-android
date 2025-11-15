package com.sharpflux.taxiapp.data.model;

public class BillRequest {
    private int RequestID;
    private int CustomersId;
    private String CustomerName;
    private String CustomerEmail;
    private String CustomerPhone;
    private String DriverName;
    private String DriverPhone;
    private String pickFrom;
    private String dropAt;
    private int distance;
    private double fare;
    private double ParkingCharges;
    private double WaitingCharges;
    private double TollTax;
    private double DriverTip;
    private String BillDate;
    private double Total;
    private boolean IsActive;
    private int approvalId;

    // Getters and Setters
    public int getRequestID() {
        return RequestID;
    }

    public void setRequestID(int requestID) {
        RequestID = requestID;
    }

    public int getCustomersId() {
        return CustomersId;
    }

    public void setCustomersId(int customersId) {
        CustomersId = customersId;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerEmail() {
        return CustomerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        CustomerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public String getDriverName() {
        return DriverName;
    }

    public void setDriverName(String driverName) {
        DriverName = driverName;
    }

    public String getDriverPhone() {
        return DriverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        DriverPhone = driverPhone;
    }

    public String getPickFrom() {
        return pickFrom;
    }

    public void setPickFrom(String pickFrom) {
        this.pickFrom = pickFrom;
    }

    public String getDropAt() {
        return dropAt;
    }

    public void setDropAt(String dropAt) {
        this.dropAt = dropAt;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public double getParkingCharges() {
        return ParkingCharges;
    }

    public void setParkingCharges(double parkingCharges) {
        ParkingCharges = parkingCharges;
    }

    public double getWaitingCharges() {
        return WaitingCharges;
    }

    public void setWaitingCharges(double waitingCharges) {
        WaitingCharges = waitingCharges;
    }

    public double getTollTax() {
        return TollTax;
    }

    public void setTollTax(double tollTax) {
        TollTax = tollTax;
    }

    public double getDriverTip() {
        return DriverTip;
    }

    public void setDriverTip(double driverTip) {
        DriverTip = driverTip;
    }

    public String getBillDate() {
        return BillDate;
    }

    public void setBillDate(String billDate) {
        BillDate = billDate;
    }

    public double getTotal() {
        return Total;
    }

    public void setTotal(double total) {
        Total = total;
    }
    public int getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(int approvalId) {
        this.approvalId = approvalId;
    }
    public boolean isActive() {
        return IsActive;
    }

    public void setActive(boolean active) {
        IsActive = active;
    }
}