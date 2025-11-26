package com.govagency.model;

import java.time.LocalDateTime;

public class Archive {
    
    public enum ArchiveType {
        DELETED_CITIZEN("Deleted Citizen"),
        COMPLETED_REQUEST("Completed Request"),
        REJECTED_REQUEST("Rejected Request"),
        REJECTED_DOCUMENT("Rejected Document");

        private final String display;

        ArchiveType(String display) {
            this. display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private final String archiveId;
    private final String entityId;
    private final ArchiveType type;
    private final String details;
    private final LocalDateTime archivedAt;
    private final String archivedBy;
    private String reason;

    public Archive(String archiveId, String entityId, ArchiveType type, 
                   String details, String archivedBy) {
        this.archiveId = archiveId;
        this.entityId = entityId;
        this.type = type;
        this.details = details;
        this.archivedAt = LocalDateTime.now();
        this.archivedBy = archivedBy;
        this.reason = "";
    }

    // Getters and Setters
    public String getArchiveId() {
        return archiveId;
    }

    public String getEntityId() {
        return entityId;
    }

    public ArchiveType getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public String getArchivedBy() {
        return archivedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason != null ? reason : "";
    }

    @Override
    public String toString() {
        return String.format(
            "Archive[ID: %s | Type: %s | Entity: %s | Archived By: %s | Date: %s]",
            archiveId, type. getDisplay(), entityId, archivedBy, archivedAt
        );
    }
}