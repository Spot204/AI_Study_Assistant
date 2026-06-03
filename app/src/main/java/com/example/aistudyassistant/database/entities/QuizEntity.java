package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "quizzes")
public class QuizEntity {
    @PrimaryKey
    @NonNull
    private String quizId;
    private String userId;
    private String title;
    private String sourceSetId;
    private String sourceDocumentId;
    private int timeLimitMinutes;
    private int bestScore;
    private String questionsJson; // Lưu array questions dưới dạng JSON string

    public QuizEntity(@NonNull String quizId, String userId, String title) {
        this.quizId = quizId;
        this.userId = userId;
        this.title = title;
    }

    @NonNull public String getQuizId() { return quizId; }
    public void setQuizId(@NonNull String quizId) { this.quizId = quizId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceSetId() { return sourceSetId; }
    public void setSourceSetId(String sourceSetId) { this.sourceSetId = sourceSetId; }
    public String getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(String sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    public String getQuestionsJson() { return questionsJson; }
    public void setQuestionsJson(String questionsJson) { this.questionsJson = questionsJson; }
}
