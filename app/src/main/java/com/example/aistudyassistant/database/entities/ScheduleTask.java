package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedule_tasks")
public class ScheduleTask {

    @PrimaryKey
    @NonNull
    private String taskId; // [ĐÃ SỬA] Đổi từ int tự tăng sang String UUID để an toàn khi đẩy lên Cloud
    private String userId; // [THÊM MỚI] Mã ID người dùng sở hữu lịch trình này để lọc trên Firebase
    private String title; // Tiêu đề nhiệm vụ/lịch trình học
    private String startTime; // Giờ bắt đầu (Ví dụ: "08:00")
    private String endTime; // Giờ kết thúc (Ví dụ: "09:30")
    private String type; // Loại nhiệm vụ: "study" | "quiz" | "flashcard" | "reading"
    private String date; // Ngày thực hiện (Ví dụ: "2026-06-04")
    private boolean isCompleted; // Trạng thái hoàn thành (True: Đã xong, False: Chưa xong)

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian chỉnh sửa trạng thái task cuối cùng
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert", "pending_update"

    // Constructor mặc định bắt buộc cho Room DB
    public ScheduleTask() {
    }

    /**
     * Constructor sử dụng khi khởi tạo tạo mới một lịch trình công việc
     */
    public ScheduleTask(@NonNull String taskId, String userId, String title, String startTime, String endTime, String type, String date) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.date = date;
        this.isCompleted = false; // Mặc định task mới tạo là chưa hoàn thành

        // Tự động cấu hình các mốc thời gian đồng bộ mặc định khi tạo mới ở local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getTaskId() { return taskId; }
    public void setTaskId(@NonNull String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}