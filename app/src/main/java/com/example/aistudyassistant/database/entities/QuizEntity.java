package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quizzes")
public class QuizEntity {
    @PrimaryKey
    @NonNull
    private String quizId;
    private String userId;
    private String title;
    private String sourceSetId;
    private String sourceDocumentId;
    private int timeLimitMinutes;
    private int bestScore;
    private String questionsJson; // Lưu array questions dưới dạng JSON string

    // 2 trường bổ sung quan trọng để phục vụ luồng đồng bộ hóa dữ liệu
    private long updatedAt;
    private String syncStatus; // Các giá trị: "synced", "pending_insert", "pending_update"

    // Constructor đầy đủ nhất để bạn sử dụng khi tạo mới một bộ Quiz từ AI sinh ra
    public QuizEntity(@NonNull String quizId, String userId, String title, String sourceSetId,
                      String sourceDocumentId, int timeLimitMinutes, int bestScore, String questionsJson) {
        this.quizId = quizId;
        this.userId = userId;
        this.title = title;
        this.sourceSetId = sourceSetId;
        this.sourceDocumentId = sourceDocumentId;
        this.timeLimitMinutes = timeLimitMinutes;
        this.bestScore = bestScore;
        this.questionsJson = questionsJson;

        // Tự động gán thời gian hiện tại của thiết bị khi tạo mới
        this.updatedAt = System.currentTimeMillis();
        // Mặc định ban đầu khi tạo ở local là đang chờ đẩy lên cloud
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter ---
    @NonNull
    public String getQuizId() { return quizId; }
    public void setQuizId(@NonNull String quizId) { this.quizId = quizId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSourceSetId() { return sourceSetId; }
    public void setSourceSetId(String sourceSetId) { this.sourceSetId = sourceSetId; }

    public String getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(String sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }

    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }

    public String getQuestionsJson() { return questionsJson; }
    public void setQuestionsJson(String questionsJson) { this.questionsJson = questionsJson; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}