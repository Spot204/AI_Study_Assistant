package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    private String notificationId; // Mã ID duy nhất của thông báo (UUID)
    private String userId; // Mã ID người dùng nhận thông báo
    private String title; // Tiêu đề thông báo
    private String message; // Nội dung chi tiết thông báo
    private String type; // Thể loại thông báo: "reminder" | "system" | "achievement"
    private boolean isRead; // Trạng thái đọc (True: đã đọc, False: chưa đọc)
    private long createdAt; // Thời điểm tạo/bắn thông báo

    // 2 trường bắt buộc bổ sung để phục vụ luồng kiểm tra đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian cập nhật trạng thái (ví dụ khi user bấm Đọc)
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert", "pending_update"

    /**
     * Constructor sử dụng khi tạo mới một thông báo
     */
    public NotificationEntity(@NonNull String notificationId, String userId, String title, String message, String type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false; // Mặc định thông báo mới tinh là chưa đọc
        this.createdAt = System.currentTimeMillis();

        // Tự động cấu hình các mốc thời gian đồng bộ mặc định khi tạo mới ở local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(@NonNull String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}