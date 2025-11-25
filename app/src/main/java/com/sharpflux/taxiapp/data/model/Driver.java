package com.sharpflux.taxiapp.data.model;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class Driver {
    private int driverId;
    private String driverCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String emailId;
    private String phoneNumber;
    private String passwordHash;
    private Uri profileImage;
    private String address;
    private int cityId;
    private int stateId;
    private int genderId;
    private int languageId;
    private boolean speak;
    private boolean understand;
    private boolean termsConditions;
    private boolean isActive;
    private String aadharNumber;
    private Uri drivingLicenseImage;
    private String drivingLicenseValidTo;
    private Uri insuranceProof;
    private String insuranceValidTo;
    private int vehicleTypeId;
    private String vehicleNumber;
    private int roleId;
    private int userId;
    private Uri rcbookImageUri;
    private Uri signatureUri;
    private boolean isVerified;
    private int statusId;
    private int verificationStatus;
    private String rejectionReason;
    private String verificationDate;
    private int verifiedBy;
    private int locationId;
    private Map<Integer, String> documentBase64Map;

    // Dynamic document storage
    private Map<Integer, Uri> documentUriMap = new HashMap<>();

    public Driver() {
    }

    // Getters and Setters
    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }
    public  String getDriverCode(){return driverCode;}
    public  void  setDriverCode(String driverCode){this.driverCode=driverCode;}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public Uri getProfileImageUri() {
        return profileImage;
    }

    public void setProfileImage(Uri profileImage) {
        this.profileImage = profileImage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getGenderId() {
        return genderId;
    }

    public void setGenderId(int genderId) {
        this.genderId = genderId;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public boolean isSpeak() {
        return speak;
    }

    public void setSpeak(boolean speak) {
        this.speak = speak;
    }

    public boolean isUnderstand() {
        return understand;
    }

    public void setUnderstand(boolean understand) {
        this.understand = understand;
    }

    public boolean isTermsConditions() {
        return termsConditions;
    }

    public void setTermsConditions(boolean termsConditions) {
        this.termsConditions = termsConditions;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public Uri getDlImageUri() {
        return drivingLicenseImage;
    }

    public void setDrivingLicenseImage(Uri drivingLicenseImage) {
        this.drivingLicenseImage = drivingLicenseImage;
    }

    public String getDlValidTo() {
        return drivingLicenseValidTo;
    }

    public void setDrivingLicenseValidTo(String drivingLicenseValidTo) {
        this.drivingLicenseValidTo = drivingLicenseValidTo;
    }

    public Uri getInsuranceImageUri() {
        return insuranceProof;
    }

    public void setInsuranceProof(Uri insuranceProof) {
        this.insuranceProof = insuranceProof;
    }

    public String getInsuranceValidTo() {
        return insuranceValidTo;
    }

    public void setInsuranceValidTo(String insuranceValidTo) {
        this.insuranceValidTo = insuranceValidTo;
    }

    public int getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(int vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Uri getRcbookImageUri() {
        return rcbookImageUri;
    }

    public void setRcbookImageUri(Uri rcbookImageUri) {
        this.rcbookImageUri = rcbookImageUri;
    }

    public Uri getSignatureUri() {
        return signatureUri;
    }

    public void setSignatureUri(Uri signatureUri) {
        this.signatureUri = signatureUri;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public int getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(int verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(String verificationDate) {
        this.verificationDate = verificationDate;
    }

    public int getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(int verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public Map<Integer, Uri> getDocumentUriMap() {
        return documentUriMap;
    }

    public void setDocumentUriMap(Map<Integer, Uri> documentUriMap) {
        this.documentUriMap = documentUriMap;
    }
    public Map<Integer, String> getDocumentBase64Map() { return documentBase64Map; }
    public void setDocumentBase64Map(Map<Integer, String> map) { this.documentBase64Map = map; }

    //endregion

    @Override
    public String toString() {
        return "Driver{" +
                "driverId=" + driverId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailId='" + emailId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", cityId=" + cityId +
                ", stateId=" + stateId +
                ", genderId=" + genderId +
                ", vehicleTypeId=" + vehicleTypeId +
                ", languageId=" + languageId +
                ", roleId=" + roleId +
                ", isVerified=" + isVerified +
                ",statusId=" + statusId +
                ", verificationStatus=" + verificationStatus +
                ", isActive=" + isActive +
                '}';
    }
}