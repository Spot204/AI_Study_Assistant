package com.example.aistudyassistant.models;

import com.google.firebase.Timestamp;

public class Flashcard {
    private String cardId;
    private String setId;
    private String term;
    private String definition;
    private String imageUrl;
    private boolean isFavorite;
    private Timestamp createdAt;

    public Flashcard() {}

    public Flashcard(String cardId, String setId, String term, String definition, String imageUrl, boolean isFavorite, Timestamp createdAt) {
        this.cardId = cardId;
        this.setId = setId;
        this.term = term;
        this.definition = definition;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
    }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
