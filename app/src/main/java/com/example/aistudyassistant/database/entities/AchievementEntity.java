package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "achievements")
public class AchievementEntity {

    @PrimaryKey
    @NonNull
    private String achievementId; // Mã ID duy nhất của danh hiệu hệ thống (Ví dụ: "ach_streak_7")
    private String title; // Tên danh hiệu hiển thị (Ví dụ: "Chiến thần học tập")
    private String description; // Mô tả cách đạt được (Ví dụ: "Duy trì học tập liên tục trong vòng 7 ngày")
    private String iconUrl; // Đường dẫn liên kết đến ảnh biểu tượng của danh hiệu
    private String requirementType; // Loại điều kiện cần: "streak" | "quiz" | "hours" | "flashcards"
    private double requirementValue; // Con số điều kiện cần đạt được để mở khóa

    /**
     * Constructor đầy đủ để khởi tạo danh hiệu hệ thống
     */
    public AchievementEntity(@NonNull String achievementId, String title, String description, String iconUrl, String requirementType, double requirementValue) {
        this.achievementId = achievementId;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.requirementType = requirementType;
        this.requirementValue = requirementValue;
    }

    /**
     * No-argument constructor required for Firestore deserialization
     */
    @androidx.room.Ignore
    public AchievementEntity() {
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getAchievementId() { return achievementId; }
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