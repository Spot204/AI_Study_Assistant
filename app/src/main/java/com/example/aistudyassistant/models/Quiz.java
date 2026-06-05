package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class Quiz {
    private String quizId;
    private String userId;
    private String title;
    private int score;
    private int totalQuestions;
    private Timestamp createdAt;

    public Quiz() {}

    public Quiz(String quizId, String userId, String title, int score, int totalQuestions, Timestamp createdAt) {
        this.quizId = quizId;
        this.userId = userId;
        this.title = title;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.createdAt = createdAt;
    }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
