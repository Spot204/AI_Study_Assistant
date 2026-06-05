package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class UserAchievement {
    private String id; // user_id + achievement_id
    private String userId;
    private String achievementId;
    private Timestamp unlockedAt;

    public UserAchievement() {}

    public UserAchievement(String id, String userId, String achievementId, Timestamp unlockedAt) {
        this.id = id;
        this.userId = userId;
        this.achievementId = achievementId;
        this.unlockedAt = unlockedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public Timestamp getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }
}
