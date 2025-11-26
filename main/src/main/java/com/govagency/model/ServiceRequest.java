package com.govagency.model;
public class ServiceRequest {
    
    public enum Status {
        REQUESTED,
        PROCESSING,
        COMPLETED,
        REJECTED
    }

    private final String id;
    private final String citizenId;
    private final String serviceType;
    private final String description;
    private Status status;
    private String adminNote;

    /**
     * Constructor - Create a new service request
     */
    public ServiceRequest(String id, String citizenId, String serviceType, String description) {
        this.id = id;
        this.citizenId = citizenId;
        this.serviceType = serviceType;
        this.description = description;
        this.status = Status.REQUESTED;
        this.adminNote = "";
    }

    // =============== GETTERS ===============

    public String getId() {
        return id;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    // =============== SETTERS ===============

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote != null ? adminNote : "";
    }

    // =============== UTILITY METHODS ===============
    public boolean isPending() {
        return status == Status.REQUESTED || status == Status.PROCESSING;
    }

    public boolean isFinalized() {
        return status == Status.COMPLETED || status == Status.REJECTED;
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id='" + id + '\'' +
                ", citizenId='" + citizenId + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", status=" + status +
                '}';
    }
}