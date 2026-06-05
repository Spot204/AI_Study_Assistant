package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class UserStats {
    private String userId;
    private int streakCount;
    private int totalFlashcards;
    private int totalQuizzes;
    private double studyHours;
    private Timestamp lastActive;

    public UserStats() {}

    public UserStats(String userId, int streakCount, int totalFlashcards, int totalQuizzes, double studyHours, Timestamp lastActive) {
        this.userId = userId;
        this.streakCount = streakCount;
        this.totalFlashcards = totalFlashcards;
        this.totalQuizzes = totalQuizzes;
        this.studyHours = studyHours;
        this.lastActive = lastActive;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public int getTotalFlashcards() { return totalFlashcards; }
    public void setTotalFlashcards(int totalFlashcards) { this.totalFlashcards = totalFlashcards; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public double getStudyHours() { return studyHours; }
    public void setStudyHours(double studyHours) { this.studyHours = studyHours; }

    public Timestamp getLastActive() { return lastActive; }
    public void setLastActive(Timestamp lastActive) { this.lastActive = lastActive; }
}
