package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "learning_goals")
public class LearningGoalEntity {
    @PrimaryKey
    @NonNull
    private String goalId;
    private String userId;
    private String title;
    private double currentValue;
    private double targetValue;
    private String unit; // "% / giờ / bài"
    private long deadline;
    private String status; // "active | completed | overdue"

    public LearningGoalEntity(@NonNull String goalId, String userId, String title, double targetValue, String unit, long deadline) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.targetValue = targetValue;
        this.unit = unit;
        this.deadline = deadline;
        this.currentValue = 0;
        this.status = "active";
    }

    @NonNull public String getGoalId() { return goalId; }
    public void setGoalId(@NonNull String goalId) { this.goalId = goalId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
