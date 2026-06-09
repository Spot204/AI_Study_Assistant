package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey
    @NonNull
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String type; // "reminder | system | achievement"
    private boolean isRead;
    private long createdAt;

    public NotificationEntity(@NonNull String notificationId, String userId, String title, String message, String type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull public String getNotificationId() { return notificationId; }
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
}
