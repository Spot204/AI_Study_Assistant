package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.QuizDao;
import com.example.aistudyassistant.database.dao.StudySetDao;
import com.example.aistudyassistant.database.entities.QuizEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizRepository {
    private static final String TAG = "QuizRepository";
    private final QuizDao quizDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    private final StudySetDao studySetDao;

    public QuizRepository(QuizDao quizDao, StudySetDao studySetDao) {
        this.quizDao = quizDao;
        this.studySetDao = studySetDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertQuiz(QuizEntity quiz) {
        if (quiz.getQuizId() == null || quiz.getQuizId().isEmpty()) {
            quiz.setQuizId(UUID.randomUUID().toString());
        }
        if (auth.getCurrentUser() != null) {
            quiz.setUserId(auth.getCurrentUser().getUid());
        }
        quiz.setUpdatedAt(System.currentTimeMillis());
        quiz.setSyncStatus("pending_insert");

        executorService.execute(() -> {
            quizDao.insertQuiz(quiz);
            syncToCloud(quiz);
        });
    }

    public void updateQuiz(QuizEntity quiz) {
        quiz.setUpdatedAt(System.currentTimeMillis());
        quiz.setSyncStatus("pending_update");

        executorService.execute(() -> {
            quizDao.updateQuiz(quiz);
            syncToCloud(quiz);
        });
    }

    private void syncToCloud(QuizEntity quiz) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("quizzes").document(quiz.getQuizId())
                .set(quiz)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    quiz.setSyncStatus("synced");
                    quizDao.updateQuiz(quiz);
                    Log.d(TAG, "Quiz synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Quiz sync failed", e));
    }

    public void syncUnsyncedQuizzes() {
        executorService.execute(() -> {
            List<QuizEntity> unsynced = quizDao.getUnsyncedQuizzes();
            if (unsynced != null && !unsynced.isEmpty()) {
                for (QuizEntity quiz : unsynced) {
                    syncToCloud(quiz);
                }
            }
        });
    }

    public void downloadNewQuizzesFromServer() {
        executorService.execute(() -> {
            String uid = auth.getUid();
            if (uid == null) return;

            long maxUpdatedAt = quizDao.getMaxUpdatedAt();

            firestore.collection("users").document(uid)
                    .collection("quizzes")
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<QuizEntity> remoteQuizzes = queryDocumentSnapshots.toObjects(QuizEntity.class);
                            for (QuizEntity remoteQuiz : remoteQuizzes) {
                                remoteQuiz.setSyncStatus("synced");
                                quizDao.insertQuiz(remoteQuiz);
                            }
                        }
                    }));
        });
    }

    // Thêm phương thức lấy danh sách Quiz VÀ StudySet để hiển thị ở màn hình Quiz
    public void getAllQuizzesAndSets(String userId, OnDataLoadedListener listener) {
        executorService.execute(() -> {
            List<QuizEntity> quizzes = quizDao.getQuizzesByUser(userId);
            List<StudySetEntity> studySets = studySetDao.getStudySetsByUser(userId);
            
            List<com.example.aistudyassistant.fragments.quiz.PracticeItem> items = new java.util.ArrayList<>();
            
            for (QuizEntity q : quizzes) {
                items.add(new com.example.aistudyassistant.fragments.quiz.PracticeItem(
                    q.getQuizId(), q.getTitle(), q.getTimeLimitMinutes() + " phút", com.example.aistudyassistant.fragments.quiz.PracticeItem.TYPE_QUIZ));
            }
            
            for (StudySetEntity s : studySets) {
                items.add(new com.example.aistudyassistant.fragments.quiz.PracticeItem(
                    s.getSetId(), s.getTitle(), "Học phần", com.example.aistudyassistant.fragments.quiz.PracticeItem.TYPE_STUDY_SET));
            }
            
            listener.onLoaded(items);
        });
    }

    public interface OnDataLoadedListener {
        void onLoaded(List<com.example.aistudyassistant.fragments.quiz.PracticeItem> items);
    }
}
