package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_sessions")
public class StudySessionEntity {

    @PrimaryKey
    @NonNull
    private String sessionId; // Mã ID duy nhất của phiên học (UUID)
    private String userId; // Mã ID người dùng tham gia phiên này
    private String type; // Loại phiên học: "quiz" | "flashcard" | "chat"
    private String referenceId; // ID trỏ đến bộ Quiz hoặc bộ Flashcard tương ứng
    private int score; // Điểm số đạt được (Nếu làm bài trắc nghiệm)
    private int durationMinutes; // Tổng thời gian ngồi học thực tế (Tính bằng phút)

    // [ĐÃ SỬA] Đổi từ List<ChatMessage> sang String JSON để Room compile được mượt mà
    private String messagesJson;

    // [BỔ SUNG] Lưu mảng câu trả lời trắc nghiệm của user dưới dạng String JSON (theo đúng sơ đồ gốc)
    private String userAnswersJson;

    private long startedAt; // Thời điểm bấm bắt đầu học
    private long endedAt; // Thời điểm kết thúc phiên học

    // [ĐÃ SỬA] Thay thế biến boolean isSynced bằng bộ đôi quản lý đồng bộ chuẩn Firebase đám mây
    private long updatedAt; // Mốc thời gian tạo/chỉnh sửa phiên học
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert"

    /**
     * Constructor chuẩn sử dụng khi người dùng bấm bắt đầu một phiên học/phiên chat mới
     */
    public StudySessionEntity(@NonNull String sessionId, String userId, String type) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.type = type;
        this.startedAt = System.currentTimeMillis();

        // Tự động cấu hình các mốc thời gian đồng bộ mặc định khi tạo mới ở local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getSessionId() { return sessionId; }
    public void setSessionId(@NonNull String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMessagesJson() { return messagesJson; }
    public void setMessagesJson(String messagesJson) { this.messagesJson = messagesJson; }

    public String getUserAnswersJson() { return userAnswersJson; }
    public void setUserAnswersJson(String userAnswersJson) { this.userAnswersJson = userAnswersJson; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getEndedAt() { return endedAt; }
    public void setEndedAt(long endedAt) { this.endedAt = endedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}