package com.example.aistudyassistant.data.model;

import com.example.aistudyassistant.database.entities.FlashcardEntity;

public class FlashcardFirestore {
    private String flashcardId;

    private String setId;
    private String userId;
    private String front;
    private String back;
    private String aiExplanation;
    private int box;
    private long nextReviewAt;
    private long updatedAt;

    public FlashcardFirestore() {}

    public FlashcardFirestore(FlashcardEntity entity) {
        this.flashcardId = entity.getFlashcardId();
        this.setId = entity.getSetId();
        this.userId = entity.getUserId();
        this.front = entity.getFront();
        this.back = entity.getBack();
        this.aiExplanation = entity.getAiExplanation();
        this.box = entity.getBox();
        this.nextReviewAt = entity.getNextReviewAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public FlashcardEntity toEntity() {
        FlashcardEntity entity = new FlashcardEntity(flashcardId, setId, userId, front, back);
        entity.setAiExplanation(this.aiExplanation);
        entity.setBox(this.box);
        entity.setNextReviewAt(this.nextReviewAt);
        entity.setUpdatedAt(this.updatedAt);
        entity.setSyncStatus("synced");
        return entity;
    }

    // --- Getters and Setters ---
    public String getFlashcardId() { return flashcardId; }
    public void setFlashcardId(String flashcardId) { this.flashcardId = flashcardId; }
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
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}