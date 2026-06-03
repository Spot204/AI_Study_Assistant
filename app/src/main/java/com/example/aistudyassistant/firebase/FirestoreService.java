package com.example.aistudyassistant.firebase;

import com.example.aistudyassistant.database.dao.StudySessionDao;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.example.aistudyassistant.database.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirestoreService {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final StudySessionDao studySessionDao;
    private final ExecutorService executorService;

    public FirestoreService(StudySessionDao studySessionDao) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.studySessionDao = studySessionDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void saveUserToFirestore(User user) {
        db.collection("users")
                .document(user.getUserId())
                .set(user, SetOptions.merge());
    }

    /**
     * Đồng bộ tất cả các phiên chưa được sync lên Firebase
     */
    public void syncUnsyncedSessions() {
        executorService.execute(() -> {
            List<StudySessionEntity> unsynced = studySessionDao.getUnsyncedSessions();
            for (StudySessionEntity session : unsynced) {
                syncStudySession(session);
            }
        });
    }

    /**
     * Đẩy một phiên học tập lên Firebase
     */
    public void syncStudySession(StudySessionEntity session) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        session.setUserId(userId);

        db.collection("users")
                .document(userId)
                .collection("study_sessions")
                .document(session.getSessionId())
                .set(session, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    session.setSynced(true);
                    executorService.execute(() -> studySessionDao.updateSession(session));
                })
                .addOnFailureListener(e -> {
                    // Log error or handle retry
                });
    }
}
