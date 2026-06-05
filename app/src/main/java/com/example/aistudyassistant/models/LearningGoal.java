package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class LearningGoal {
    private String goalId;
    private String userId;
    private String title;
    private int currentValue;
    private int targetValue;
    private String unit;
    private Timestamp deadline;
    private String status;

    public LearningGoal() {}

    public LearningGoal(String goalId, String userId, String title, int currentValue, int targetValue, String unit, Timestamp deadline, String status) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
        this.unit = unit;
        this.deadline = deadline;
        this.status = status;
    }

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCurrentValue() { return currentValue; }
    public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
