package com.example.aistudyassistant.data.model;

import com.example.aistudyassistant.database.entities.StudySetEntity;

public class StudySetFirestore {
    private String setId;

    private String userId;
    private String title;
    private String visibility;
    private int totalCards;
    private float masteryPercentage;
    private long updatedAt;

    public StudySetFirestore() {}

    public StudySetFirestore(StudySetEntity entity) {
        this.setId = entity.getSetId();
        this.userId = entity.getUserId();
        this.title = entity.getTitle();
        this.visibility = entity.getVisibility();
        this.totalCards = entity.getTotalCards();
        this.masteryPercentage = entity.getMasteryPercentage();
        this.updatedAt = entity.getUpdatedAt();
    }

    public StudySetEntity toEntity() {
        StudySetEntity entity = new StudySetEntity(setId, userId, title);
        entity.setVisibility(this.visibility);
        entity.setTotalCards(this.totalCards);
        entity.setMasteryPercentage(this.masteryPercentage);
        entity.setUpdatedAt(this.updatedAt);
        entity.setSyncStatus("synced");
        return entity;
    }

    // --- Getters and Setters ---
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public int getTotalCards() { return totalCards; }
    public void setTotalCards(int totalCards) { this.totalCards = totalCards; }
    public float getMasteryPercentage() { return masteryPercentage; }
    public void setMasteryPercentage(float masteryPercentage) { this.masteryPercentage = masteryPercentage; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}