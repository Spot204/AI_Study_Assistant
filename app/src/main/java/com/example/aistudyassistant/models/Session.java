package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class Session {
    private String sessionId;
    private String userId;
    private String setId;
    private Timestamp startTime;
    private Timestamp endTime;
    private int duration; // in minutes
    private String type; // study | quiz

    public Session() {}

    public Session(String sessionId, String userId, String setId, Timestamp startTime, Timestamp endTime, int duration, String type) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.setId = setId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.type = type;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
