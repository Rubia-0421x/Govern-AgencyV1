package com.govagency.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Document {
    public enum Status {
        PENDING("⏳ Pending Review"),
        APPROVED("✅ Approved"),
        REJECTED("❌ Rejected");

        private final String display;

        Status(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private final String documentId;
    private final String attachedRequestId;
    private final String citizenId;
    private final String filePath;
    private Status status;
    private String reviewComment;
    private final LocalDateTime uploadTime;
    private LocalDateTime reviewTime;

    public Document(String documentId, String requestId, String filePath, String citizenId) {
        this.documentId = documentId;
        this.attachedRequestId = requestId;
        this.filePath = filePath;
        this.citizenId = citizenId;
        this.status = Status.PENDING;
        this.uploadTime = LocalDateTime.now();
        this.reviewComment = "";
    }

    // Getters and Setters
    public String getId() {
        return documentId;
    }

    public String getAttachedRequestId() {
        return attachedRequestId;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public String getFilePath() {
        return filePath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.reviewTime = LocalDateTime.now();
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment != null ? reviewComment : "";
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    @Override
    public String toString() {
        return String.format(
            "Doc ID: %s | Request: %s | File: %s | Status: %s | Uploaded: %s",
            documentId,
            attachedRequestId,
            filePath,
            status.getDisplay(),
            uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}