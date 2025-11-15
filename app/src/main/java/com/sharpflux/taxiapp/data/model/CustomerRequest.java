package com.sharpflux.taxiapp.data.model;
public class CustomerRequest {
    private int requestID;
    private int customersId;
    private String pickFrom;
    private String dropAt;
    private double distance;
    private double fare;

    public CustomerRequest(int requestID, String pickFrom, String dropAt, double distance, double fare) {
        this.requestID = requestID;
        this.customersId = requestID; // Just for example
        this.pickFrom = pickFrom;
        this.dropAt = dropAt;
        this.distance = distance;
        this.fare = fare;
    }

    public int getRequestID() { return requestID; }
    public int getCustomersId() { return customersId; }
    public String getPickFrom() { return pickFrom; }
    public String getDropAt() { return dropAt; }
    public double getDistance() { return distance; }
    public double getFare() { return fare; }
}
