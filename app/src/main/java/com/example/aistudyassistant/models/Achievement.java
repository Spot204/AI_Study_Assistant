package com.example.aistudyassistant.models;

public class Achievement {
    private String achievementId;
    private String title;
    private String description;
    private String iconUrl;
    private String requirementType;
    private int requirementValue;

    public Achievement() {}

    public Achievement(String achievementId, String title, String description, String iconUrl, String requirementType, int requirementValue) {
        this.achievementId = achievementId;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.requirementType = requirementType;
        this.requirementValue = requirementValue;
    }

    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getRequirementType() { return requirementType; }
    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }

    public int getRequirementValue() { return requirementValue; }
    public void setRequirementValue(int requirementValue) { this.requirementValue = requirementValue; }
}
