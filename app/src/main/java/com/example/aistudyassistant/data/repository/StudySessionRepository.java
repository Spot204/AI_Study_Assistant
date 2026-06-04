package com.example.aistudyassistant.data.repository;

import com.example.aistudyassistant.data.model.StudySessionFirestore;
import com.example.aistudyassistant.database.dao.StudySessionDao;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudySessionRepository {
    private final StudySessionDao studySessionDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public StudySessionRepository(StudySessionDao studySessionDao) {
        this.studySessionDao = studySessionDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Hàm helper để lấy đường dẫn Collection chuẩn của Firebase cho User hiện tại
     */
    private String getUserCollectionPath() {
        if (auth.getCurrentUser() != null) {
            return "users/" + auth.getCurrentUser().getUid() + "/study_sessions";
        }
        return null;
    }

    /**
     * Thêm một phiên học/chat mới (Offline-First)
     */
    public void insertSession(StudySessionEntity session) {
        executorService.execute(() -> {
            // Bước 1: Lưu tức thì xuống SQLite để hiển thị UI không độ trễ
            studySessionDao.insertSession(session);

            // Bước 2: Thử đẩy lên Firebase Cloud
            String collectionPath = getUserCollectionPath();
            if (collectionPath != null) {
                StudySessionFirestore cloudModel = new StudySessionFirestore(session);
                firestore.collection(collectionPath)
                        .document(session.getSessionId())
                        .set(cloudModel)
                        .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                            session.setSyncStatus("synced");
                            studySessionDao.updateSession(session);
                        }));
            }
        });
    }

    /**
     * Cập nhật phiên học (Ví dụ: Thêm tin nhắn mới vào phiên chat)
     */
    public void updateSession(StudySessionEntity session) {
        session.setUpdatedAt(System.currentTimeMillis());
        session.setSyncStatus("pending_update");

        executorService.execute(() -> {
            studySessionDao.updateSession(session);

            String collectionPath = getUserCollectionPath();
            if (collectionPath != null) {
                StudySessionFirestore cloudModel = new StudySessionFirestore(session);
                firestore.collection(collectionPath)
                        .document(session.getSessionId())
                        .set(cloudModel)
                        .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                            session.setSyncStatus("synced");
                            studySessionDao.updateSession(session);
                        }));
            }
        });
    }

    /**
     * Xóa một phiên học cụ thể
     */
    public void deleteSession(StudySessionEntity session) {
        executorService.execute(() -> {
            studySessionDao.deleteSession(session);

            String collectionPath = getUserCollectionPath();
            if (collectionPath != null) {
                firestore.collection(collectionPath)
                        .document(session.getSessionId())
                        .delete();
            }
        });
    }

    // =================================================================
    // 💥 LUỒNG ĐỒNG BỘ NGẦM TỰ ĐỘNG
    // =================================================================

    /**
     * HÀM UPLOAD: Quét các phiên học/chat chưa được đồng bộ (đang treo dưới máy) và đẩy lên Cloud
     */
    public void syncUnsyncedSessions() {
        executorService.execute(() -> {
            List<StudySessionEntity> unsyncedList = studySessionDao.getUnsyncedSessions();
            if (unsyncedList == null || unsyncedList.isEmpty()) return;

            String collectionPath = getUserCollectionPath();
            if (collectionPath == null) return;

            WriteBatch batch = firestore.batch();
            List<StudySessionEntity> sessionsToUpdateLocal = new ArrayList<>();

            for (StudySessionEntity session : unsyncedList) {
                StudySessionFirestore dto = new StudySessionFirestore(session);
                batch.set(firestore.collection(collectionPath).document(session.getSessionId()), dto);

                session.setSyncStatus("synced");
                sessionsToUpdateLocal.add(session);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                for (StudySessionEntity session : sessionsToUpdateLocal) {
                    studySessionDao.updateSession(session);
                }
            }));
        });
    }

    /**
     * HÀM DOWNLOAD: Lấy dữ liệu phiên học từ đám mây về máy
     * Rất hữu ích khi user đăng nhập trên một chiếc điện thoại mới
     */
    public void downloadNewSessionsFromServer() {
        executorService.execute(() -> {
            String collectionPath = getUserCollectionPath();
            if (collectionPath == null) return;

            // Tìm mốc thời gian cập nhật lớn nhất dưới máy hiện tại
            long maxUpdatedAt = studySessionDao.getMaxUpdatedAt();

            firestore.collection(collectionPath)
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<StudySessionFirestore> remoteSessions = queryDocumentSnapshots.toObjects(StudySessionFirestore.class);
                            for (StudySessionFirestore remoteSession : remoteSessions) {
                                studySessionDao.insertSession(remoteSession.toEntity());
                            }
                        }
                    }));
        });
    }
}