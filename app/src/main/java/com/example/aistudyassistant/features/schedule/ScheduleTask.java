package com.example.aistudyassistant.features.schedule;

public class ScheduleTask {
    private String id;
    private String title;
    private String startTime;
    private String endTime;
    private String type;
    private boolean isCompleted;

    public ScheduleTask() {
    }

    public ScheduleTask(String title, String startTime, String endTime, String type) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.isCompleted = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
