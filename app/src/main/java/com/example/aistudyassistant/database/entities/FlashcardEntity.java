package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class FlashcardEntity {
    @PrimaryKey
    @NonNull
    private String flashcardId;
    private String setId;
    private String userId;
    private String front;
    private String back;
    private String aiExplanation;
    private int box;
    private long nextReviewAt;

    public FlashcardEntity(@NonNull String flashcardId, String setId, String userId, String front, String back) {
        this.flashcardId = flashcardId;
        this.setId = setId;
        this.userId = userId;
        this.front = front;
        this.back = back;
        this.box = 1;
        this.nextReviewAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getFlashcardId() { return flashcardId; }
    public void setFlashcardId(@NonNull String flashcardId) { this.flashcardId = flashcardId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public int getBox() { return box; }
    public void setBox(int box) { this.box = box; }

    public long getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(long nextReviewAt) { this.nextReviewAt = nextReviewAt; }
}
