package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_stats")
public class UserStatsEntity {

    @PrimaryKey
    @NonNull
    private String userId; // Mã ID duy nhất của người dùng (Nên lấy trùng với Firebase UID)
    private int streakCount; // Số ngày học liên tục (Streak)
    private int totalStudyDays; // Tổng số ngày đã học (Bao nhiêu ngày)
    private int totalFlashcards; // Tổng số thẻ flashcard đã tạo
    private int totalQuizzes; // Tổng số bộ quiz đã làm/tạo
    private double studyHours; // Tổng số giờ đã tích lũy học trên app
    private long lastActive; // Mốc thời gian cuối cùng có hành động tương tác với app (Timestamp)

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa chỉ số lên Firebase Cloud
    private long updatedAt; // Mốc thời gian cập nhật chỉ số cuối cùng
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_update"

    /**
     * Constructor thực tế của bạn, tự động khởi tạo các chỉ số từ số 0 cho người dùng mới
     */
    public UserStatsEntity(@NonNull String userId) {
        this.userId = userId;
        this.streakCount = 0;
        this.totalStudyDays = 0;
        this.totalFlashcards = 0;
        this.totalQuizzes = 0;
        this.studyHours = 0.0;
        this.lastActive = 0; // Khởi tạo bằng 0 để bài học đầu tiên được tính là ngày 1

        // Tự động cấu hình mốc thời gian đồng bộ mặc định khi tạo mới bản ghi local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_update";
    }

    /**
     * No-argument constructor required for Firestore deserialization
     */
    @androidx.room.Ignore
    public UserStatsEntity() {
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public int getTotalStudyDays() { return totalStudyDays; }
    public void setTotalStudyDays(int totalStudyDays) { this.totalStudyDays = totalStudyDays; }

    public int getTotalFlashcards() { return totalFlashcards; }
    public void setTotalFlashcards(int totalFlashcards) { this.totalFlashcards = totalFlashcards; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
