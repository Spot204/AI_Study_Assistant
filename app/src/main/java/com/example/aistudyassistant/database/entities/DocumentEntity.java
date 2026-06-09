package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "documents")
public class DocumentEntity {
    @PrimaryKey
    @NonNull
    private String documentId;
    private String userId;
    private String storageUri;
    private String ocrTextUri;
    private String summary;
    private long uploadedAt;

    public DocumentEntity(@NonNull String documentId, String userId, String storageUri) {
        this.documentId = documentId;
        this.userId = userId;
        this.storageUri = storageUri;
        this.uploadedAt = System.currentTimeMillis();
    }

    @NonNull public String getDocumentId() { return documentId; }
    public void setDocumentId(@NonNull String documentId) { this.documentId = documentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStorageUri() { return storageUri; }
    public void setStorageUri(String storageUri) { this.storageUri = storageUri; }
    public String getOcrTextUri() { return ocrTextUri; }
    public void setOcrTextUri(String ocrTextUri) { this.ocrTextUri = ocrTextUri; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public long getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(long uploadedAt) { this.uploadedAt = uploadedAt; }
}
