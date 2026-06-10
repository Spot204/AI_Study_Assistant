package com.example.aistudyassistant.features.quiz;

import android.content.Context;
import com.example.aistudyassistant.data.repository.QuizRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.QuizEntity;

public class QuizController {
    private final QuizRepository quizRepository;

    public QuizController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.quizRepository = new QuizRepository(db.quizDao());
    }

    public void saveQuiz(QuizEntity quiz) {
        quizRepository.insertQuiz(quiz);
    }

    public void updateQuiz(QuizEntity quiz) {
        quizRepository.updateQuiz(quiz);
    }

    public void syncQuizzes() {
        quizRepository.syncUnsyncedQuizzes();
        quizRepository.downloadNewQuizzesFromServer();
    }
}
