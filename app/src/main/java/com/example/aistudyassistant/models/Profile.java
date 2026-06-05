package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;
import java.util.Map;

public class Profile {
    private String userId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private boolean isPremium;
    private Timestamp createdAt;
    private String subscriptionTier;
    private Map<String, Object> stats;
    private Map<String, Object> settings;

    public Profile() {}

    public Profile(String userId, String fullName, String avatarUrl, String bio, boolean isPremium, Timestamp createdAt, String subscriptionTier, Map<String, Object> stats, Map<String, Object> settings) {
        this.userId = userId;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.isPremium = isPremium;
        this.createdAt = createdAt;
        this.subscriptionTier = subscriptionTier;
        this.stats = stats;
        this.settings = settings;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }
}
