package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "documents")
public class DocumentEntity {

    @PrimaryKey
    @NonNull
    private String documentId; // Mã ID duy nhất của file tài liệu (UUID)
    private String userId; // Mã ID người dùng sở hữu tài liệu này
    private String storageUri; // Đường dẫn URL trỏ đến file gốc (.pdf, .png...) lưu trên Firebase Storage
    private String ocrTextUri; // Đường dẫn hoặc nội dung văn bản thô sau khi đã quét trích xuất chữ (OCR)
    private String summary; // Đoạn văn bản ngắn tóm tắt nội dung chính của tài liệu do AI xử lý
    private long uploadedAt; // Mốc thời gian tải file lên hệ thống

    // 2 trường bắt buộc bổ sung để phục vụ luồng kiểm tra đồng bộ danh sách tài liệu
    private long updatedAt; // Mốc thời gian chỉnh sửa/cập nhật cuối cùng
    private String syncStatus; // Trạng thái đồng bộ danh mục: "synced", "pending_insert", "pending_update"

    /**
     * Constructor để sử dụng khi người dùng bấm chọn tải lên một tài liệu mới
     */
    public DocumentEntity(@NonNull String documentId, String userId, String storageUri) {
        this.documentId = documentId;
        this.userId = userId;
        this.storageUri = storageUri;
        this.uploadedAt = System.currentTimeMillis();

        // Tự động cấu hình các mốc thời gian đồng bộ mặc định khi tạo mới ở local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getDocumentId() { return documentId; }
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

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}