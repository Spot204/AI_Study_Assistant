package com.example.aistudyassistant.data.repository;

import com.example.aistudyassistant.data.model.StudySessionFirestore;
import com.example.aistudyassistant.database.dao.StudySessionDao;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudySessionRepository {
    private static final String TAG = "StudySessionRepository";
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
        String currentUid = auth.getUid();
        if (session.getUserId() == null || session.getUserId().isEmpty()) {
            if (currentUid != null) session.setUserId(currentUid);
        }

        if (session.getSessionId() == null || session.getSessionId().isEmpty()) {
            session.setSessionId(java.util.UUID.randomUUID().toString());
        }
        session.setUpdatedAt(System.currentTimeMillis());
        session.setSyncStatus("pending_insert");

        Log.d(TAG, "Inserting new session: " + session.getSessionId());
        executorService.execute(() -> {
            // 1. Lưu SQLite trước
            studySessionDao.insertSession(session);

            // 2. Thử đẩy lên Firebase Cloud
            syncSessionToCloud(session);
        });
    }

    private void syncSessionToCloud(StudySessionEntity session) {
        String collectionPath = getUserCollectionPath();
        Log.d(TAG, "DEBUG_PATH: " + collectionPath);
        if (collectionPath != null) {
            StudySessionFirestore cloudModel = new StudySessionFirestore(session);
            firestore.collection(collectionPath)
                    .document(session.getSessionId())
                    .set(cloudModel)
                    .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                        session.setSyncStatus("synced");
                        studySessionDao.updateSession(session);
                        Log.i(TAG, "Session synced: " + session.getSessionId());
                    }))
                    .addOnFailureListener(e -> Log.e(TAG, "Sync failed for " + session.getSessionId(), e));
        }
    }

    /**
     * Cập nhật phiên học (Ví dụ: Thêm tin nhắn mới vào phiên chat)
     */
    public void updateSession(StudySessionEntity session) {
        Log.d(TAG, "Updating session: " + session.getSessionId());
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
                            Log.i(TAG, "Session update synced: " + session.getSessionId());
                            session.setSyncStatus("synced");
                            studySessionDao.updateSession(session);
                        }))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync session update: " + session.getSessionId(), e));
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
        Log.i(TAG, "Starting background sync for unsynced sessions...");
        executorService.execute(() -> {
            List<StudySessionEntity> unsyncedList = studySessionDao.getUnsyncedSessions();
            if (unsyncedList == null || unsyncedList.isEmpty()) {
                Log.d(TAG, "No unsynced sessions found.");
                return;
            }

            Log.d(TAG, "Found " + unsyncedList.size() + " unsynced sessions.");
            String collectionPath = getUserCollectionPath();
            if (collectionPath == null) {
                Log.w(TAG, "Sync failed: User not logged in");
                return;
            }

            WriteBatch batch = firestore.batch();
            List<StudySessionEntity> sessionsToUpdateLocal = new ArrayList<>();

            for (StudySessionEntity session : unsyncedList) {
                StudySessionFirestore dto = new StudySessionFirestore(session);
                batch.set(firestore.collection(collectionPath).document(session.getSessionId()), dto);

                session.setSyncStatus("synced");
                sessionsToUpdateLocal.add(session);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                Log.i(TAG, "Batch sync successful for " + sessionsToUpdateLocal.size() + " sessions.");
                for (StudySessionEntity session : sessionsToUpdateLocal) {
                    studySessionDao.updateSession(session);
                }
            })).addOnFailureListener(e -> Log.e(TAG, "Batch sync failed", e));
        });
    }

    /**
     * HÀM DOWNLOAD: Lấy dữ liệu phiên học từ đám mây về máy (Đồng bộ cho Worker)
     */
    public void downloadNewSessionsFromServer() {
        String collectionPath = getUserCollectionPath();
        if (collectionPath == null) return;

        // Tìm mốc thời gian cập nhật lớn nhất dưới máy hiện tại
        long maxUpdatedAt = studySessionDao.getMaxUpdatedAt();

        try {
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task = 
                firestore.collection(collectionPath)
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get();
            
            com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots = com.google.android.gms.tasks.Tasks.await(task);
            
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                List<StudySessionFirestore> remoteSessions = queryDocumentSnapshots.toObjects(StudySessionFirestore.class);
                for (StudySessionFirestore remoteSession : remoteSessions) {
                    StudySessionEntity entity = remoteSession.toEntity();
                    entity.setSyncStatus("synced");
                    studySessionDao.insertSession(entity);
                    Log.d(TAG, "Downloaded session: " + entity.getSessionId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải lịch sử phiên học: " + e.getMessage());
        }
    }
}