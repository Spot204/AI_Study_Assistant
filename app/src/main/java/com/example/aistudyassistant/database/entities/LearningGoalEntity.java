package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "learning_goals")
public class LearningGoalEntity {

    @PrimaryKey
    @NonNull
    private String goalId; // Mã ID duy nhất của mục tiêu (UUID String)
    private String userId; // Mã ID của user sở hữu mục tiêu này
    private String title; // Tiêu đề mục tiêu (Ví dụ: "Học từ vựng mỗi ngày")
    private double currentValue; // Tiến độ hiện tại đạt được (Ví dụ: đã học 1.5 giờ)
    private double targetValue; // Con số mục tiêu cần đạt (Ví dụ: mục tiêu là 3 giờ)
    private String unit; // Đơn vị đo lường: "%", "giờ", "bài"
    private long deadline; // Hạn chót hoàn thành mục tiêu (Timestamp)
    private String status; // Trạng thái: "active" | "completed" | "overdue"

    // 2 trường bắt buộc để phục vụ luồng đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian chỉnh sửa cuối cùng
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert", "pending_update"

    /**
     * Constructor đầy đủ để khởi tạo một mục tiêu học tập mới
     */
    public LearningGoalEntity(@NonNull String goalId, String userId, String title, double currentValue,
                              double targetValue, String unit, long deadline, String status) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
        this.unit = unit;
        this.deadline = deadline;
        this.status = status;

        // Tự động gán thời gian hiện tại và đánh dấu chờ sync lên Cloud
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getGoalId() { return goalId; }
    public void setGoalId(@NonNull String goalId) { this.goalId = goalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; } // Đã sửa lỗi thừa ký tự tại đây

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}