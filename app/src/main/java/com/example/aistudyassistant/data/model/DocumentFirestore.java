package com.example.aistudyassistant.data.model;

import com.example.aistudyassistant.database.entities.DocumentEntity;

public class DocumentFirestore {
    private String documentId;
    private String userId;
    private String storageUri; // Đã sửa: Khớp đúng trường của bạn
    private String ocrTextUri; // Đã sửa: Khớp đúng trường của bạn
    private String summary;
    private long uploadedAt;
    private long updatedAt;

    // Constructor trống bắt buộc cho Firebase
    public DocumentFirestore() {}

    /**
     * Chuyển đổi từ Local Entity (Room) sang Cloud Model (Firestore DTO)
     */
    public DocumentFirestore(DocumentEntity entity) {
        this.documentId = entity.getDocumentId();
        this.userId = entity.getUserId();
        this.storageUri = entity.getStorageUri();   // Đóng gói đúng trường storageUri
        this.ocrTextUri = entity.getOcrTextUri();   // Đóng gói đúng trường ocrTextUri
        this.summary = entity.getSummary();
        this.uploadedAt = entity.getUploadedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    /**
     * Giải nén từ Cloud Model (Firestore DTO) ngược về Local Entity (Room) để lưu SQLite
     */
    public DocumentEntity toEntity() {
        // Khởi tạo lại đúng Constructor thực tế của bạn
        DocumentEntity entity = new DocumentEntity(documentId, userId, storageUri);
        entity.setOcrTextUri(this.ocrTextUri); // Bổ sung trường này
        entity.setSummary(this.summary);
        entity.setUploadedAt(this.uploadedAt);
        entity.setUpdatedAt(this.updatedAt);
        entity.setSyncStatus("synced"); // Tải từ Cloud về mặc định đã đồng bộ xong
        return entity;
    }

    // --- Hệ thống Getter và Setter dành cho Firestore ---
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

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

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}