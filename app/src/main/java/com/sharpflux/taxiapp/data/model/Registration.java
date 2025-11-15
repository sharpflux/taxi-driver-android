package com.sharpflux.taxiapp.data.model;

public class Registration {
    private int driverId;
    private int locationId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String emailId;
    private String phoneNumber;
    private String passwordHash;
    private String address;
    private int cityId;
    private int stateId;
    private int roleId;
    private boolean isActive;
    private int userId;

    // Default constructor
    public Registration() {
        this.isActive = true; // Default value
        this.roleId = 3;
    }

    // Constructor with all fields
    public Registration(int driverId, int locationId, String firstName, String middleName,
                String lastName, String emailId, String phoneNumber, String passwordHash,
                String address, int cityId, int stateId, int roleId, boolean isActive, int userId) {
        this.driverId = driverId;
        this.locationId = locationId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.emailId = emailId;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.address = address;
        this.cityId = cityId;
        this.stateId = stateId;
        this.roleId = roleId;
        this.isActive = isActive;
        this.userId = userId;
    }

    // Getters
    public int getCustomersId() {
        return driverId;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public int getCityId() {
        return cityId;
    }

    public int getStateId() {
        return stateId;
    }

    public int getRoleId() {
        return roleId;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getUserId() {
        return userId;
    }

    // Setters
    public void setCustomersId(int driverId) {
        this.driverId = driverId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "driverId=" + driverId +
                ", locationId=" + locationId +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailId='" + emailId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", cityId=" + cityId +
                ", stateId=" + stateId +
                ", roleId=" + roleId +
                ", isActive=" + isActive +
                ", userId=" + userId +
                '}';
    }
}