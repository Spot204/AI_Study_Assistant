package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_stats")
public class UserStatsEntity {
    @PrimaryKey
    @NonNull
    private String userId;
    private int streakCount;
    private int totalFlashcards;
    private int totalQuizzes;
    private double studyHours;
    private long lastActive;

    public UserStatsEntity(@NonNull String userId) {
        this.userId = userId;
        this.streakCount = 0;
        this.totalFlashcards = 0;
        this.totalQuizzes = 0;
        this.studyHours = 0.0;
        this.lastActive = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }
    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
    public int getTotalFlashcards() { return totalFlashcards; }
    public void setTotalFlashcards(int totalFlashcards) { this.totalFlashcards = totalFlashcards; }
    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }
    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }
    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }
}
