package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class Document {
    private String documentId;
    private String userId;
    private String title;
    private String fileUrl;
    private String contentSummary;
    private Timestamp createdAt;

    public Document() {}

    public Document(String documentId, String userId, String title, String fileUrl, String contentSummary, Timestamp createdAt) {
        this.documentId = documentId;
        this.userId = userId;
        this.title = title;
        this.fileUrl = fileUrl;
        this.contentSummary = contentSummary;
        this.createdAt = createdAt;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getContentSummary() { return contentSummary; }
    public void setContentSummary(String contentSummary) { this.contentSummary = contentSummary; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
