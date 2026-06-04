package com.example.aistudyassistant.data.model;

import com.example.aistudyassistant.database.entities.StudySessionEntity;

public class StudySessionFirestore {
    private String sessionId;
    private String userId;
    private String type;
    private String referenceId;
    private int score;
    private int durationMinutes;
    private String messagesJson; // Đã cấu hình ép chuỗi JSON an toàn với Room
    private String userAnswersJson;
    private long startedAt;
    private long endedAt;
    private long updatedAt;

    public StudySessionFirestore() {}

    public StudySessionFirestore(StudySessionEntity entity) {
        this.sessionId = entity.getSessionId();
        this.userId = entity.getUserId();
        this.type = entity.getType();
        this.referenceId = entity.getReferenceId();
        this.score = entity.getScore();
        this.durationMinutes = entity.getDurationMinutes();
        this.messagesJson = entity.getMessagesJson();
        this.userAnswersJson = entity.getUserAnswersJson();
        this.startedAt = entity.getStartedAt();
        this.endedAt = entity.getEndedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public StudySessionEntity toEntity() {
        StudySessionEntity entity = new StudySessionEntity(sessionId, userId, type);
        entity.setReferenceId(this.referenceId);
        entity.setScore(this.score);
        entity.setDurationMinutes(this.durationMinutes);
        entity.setMessagesJson(this.messagesJson);
        entity.setUserAnswersJson(this.userAnswersJson);
        entity.setStartedAt(this.startedAt);
        entity.setEndedAt(this.endedAt);
        entity.setUpdatedAt(this.updatedAt);
        entity.setSyncStatus("synced");
        return entity;
    }

    // --- Getters and Setters ---
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
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
}