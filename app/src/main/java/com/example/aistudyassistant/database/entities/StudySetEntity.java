package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_sets")
public class StudySetEntity {
    @PrimaryKey
    @NonNull
    private String setId;
    private String userId;
    private String title;
    private String visibility; // "public" | "private"
    private int totalCards;
    private float masteryPercentage;

    public StudySetEntity(@NonNull String setId, String userId, String title) {
        this.setId = setId;
        this.userId = userId;
        this.title = title;
        this.visibility = "private";
    }

    // Getters and Setters
    @NonNull
    public String getSetId() { return setId; }
    public void setSetId(@NonNull String setId) { this.setId = setId; }

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
}
