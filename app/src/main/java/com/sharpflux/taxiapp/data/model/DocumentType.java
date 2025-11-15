package com.sharpflux.taxiapp.data.model;

import com.google.gson.annotations.SerializedName;

public class DocumentType {

    @SerializedName("DocumentTypeId")
    private int documentTypeId;

    @SerializedName("DocumentName")
    private String documentName;

    @SerializedName("IsActive")
    private boolean isActive;

    public DocumentType() {
    }

    public DocumentType(int documentTypeId, String documentName, boolean isActive) {
        this.documentTypeId = documentTypeId;
        this.documentName = documentName;
        this.isActive = isActive;
    }

    public int getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(int documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "DocumentType{" +
                "documentTypeId=" + documentTypeId +
                ", documentName='" + documentName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}