package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class StudySet {
    private String setId;
    private String userId;
    private String title;
    private String category;
    private int cardCount;
    private Timestamp createdAt;

    public StudySet() {}

    public StudySet(String setId, String userId, String title, String category, int cardCount, Timestamp createdAt) {
        this.setId = setId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.cardCount = cardCount;
        this.createdAt = createdAt;
    }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCardCount() { return cardCount; }
    public void setCardCount(int cardCount) { this.cardCount = cardCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
