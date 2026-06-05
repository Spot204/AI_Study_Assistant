package com.example.aistudyassistant.models;

import java.util.List;

public class Question {
    private String questionId;
    private String quizId;
    private String text;
    private List<String> options;
    private int correctAnswer;

    public Question() {}

    public Question(String questionId, String quizId, String text, List<String> options, int correctAnswer) {
        this.questionId = questionId;
        this.quizId = quizId;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
}
