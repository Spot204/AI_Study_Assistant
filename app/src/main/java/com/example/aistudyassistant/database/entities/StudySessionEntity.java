package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "study_sessions")
public class StudySessionEntity {
    @PrimaryKey
    @NonNull
    private String sessionId;
    private String userId;
    private String type; // "quiz", "flashcard", "chat"
    private String referenceId;
    private int score;
    private int durationMinutes;
    private List<ChatMessage> messages;
    private long startedAt;
    private long endedAt;
    private boolean isSynced;

    public StudySessionEntity(@NonNull String sessionId, String userId, String type) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.type = type;
        this.startedAt = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Getters and Setters
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

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getEndedAt() { return endedAt; }
    public void setEndedAt(long endedAt) { this.endedAt = endedAt; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
