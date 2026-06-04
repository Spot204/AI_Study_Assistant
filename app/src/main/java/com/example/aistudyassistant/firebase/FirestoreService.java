package com.example.aistudyassistant.firebase;

import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class FirestoreService {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Đẩy một phiên học tập (hoặc Chat) lên Firebase
     */
    public void syncStudySession(StudySessionEntity session) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        session.setUserId(userId);

        db.collection("users")
                .document(userId)
                .collection("study_sessions")
                .document(session.getSessionId())
                .set(session, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Đánh dấu đã đồng bộ thành công (Cần xử lý callback để update SQLite sau)
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                });
    }

    // Bạn có thể thêm các hàm syncStudySet, syncFlashcards tương tự ở đây...
}
