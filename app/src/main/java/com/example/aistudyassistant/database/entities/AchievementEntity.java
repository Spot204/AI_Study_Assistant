package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "achievements")
public class AchievementEntity {
    @PrimaryKey
    @NonNull
    private String achievementId;
    private String title;
    private String description;
    private String iconUrl;
    private String requirementType; // "streak | quiz | hours | flashcards"
    private double requirementValue;

    public AchievementEntity(@NonNull String achievementId, String title, String description, String iconUrl, String requirementType, double requirementValue) {
        this.achievementId = achievementId;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.requirementType = requirementType;
        this.requirementValue = requirementValue;
    }

    @NonNull public String getAchievementId() { return achievementId; }
    public void setAchievementId(@NonNull String achievementId) { this.achievementId = achievementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getRequirementType() { return requirementType; }
    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }
    public double getRequirementValue() { return requirementValue; }
    public void setRequirementValue(double requirementValue) { this.requirementValue = requirementValue; }
}
