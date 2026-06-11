package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.LearningGoalDao;
import com.example.aistudyassistant.database.entities.LearningGoalEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LearningGoalRepository {
    private static final String TAG = "LearningGoalRepository";
    private final LearningGoalDao learningGoalDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public LearningGoalRepository(LearningGoalDao learningGoalDao) {
        this.learningGoalDao = learningGoalDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertGoal(LearningGoalEntity goal) {
        if (goal.getGoalId() == null || goal.getGoalId().isEmpty()) {
            goal.setGoalId(UUID.randomUUID().toString());
        }
        if (auth.getCurrentUser() != null) {
            goal.setUserId(auth.getCurrentUser().getUid());
        }
        goal.setUpdatedAt(System.currentTimeMillis());
        goal.setSyncStatus("pending_insert");

        executorService.execute(() -> {
            learningGoalDao.insertGoal(goal);
            syncToCloud(goal);
        });
    }

    public void updateGoal(LearningGoalEntity goal) {
        goal.setUpdatedAt(System.currentTimeMillis());
        goal.setSyncStatus("pending_update");

        executorService.execute(() -> {
            learningGoalDao.updateGoal(goal);
            syncToCloud(goal);
        });
    }

    public void deleteGoal(LearningGoalEntity goal) {
        executorService.execute(() -> {
            learningGoalDao.deleteGoal(goal);
            String uid = auth.getUid();
            if (uid != null) {
                firestore.collection("users").document(uid)
                        .collection("learning_goals").document(goal.getGoalId())
                        .delete();
            }
        });
    }

    private void syncToCloud(LearningGoalEntity goal) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("learning_goals").document(goal.getGoalId())
                .set(goal)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    goal.setSyncStatus("synced");
                    learningGoalDao.updateGoal(goal);
                    Log.d(TAG, "LearningGoal synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "LearningGoal sync failed", e));
    }

    public void syncUnsyncedGoals() {
        executorService.execute(() -> {
            List<LearningGoalEntity> unsynced = learningGoalDao.getUnsyncedGoals();
            if (unsynced != null && !unsynced.isEmpty()) {
                for (LearningGoalEntity goal : unsynced) {
                    syncToCloud(goal);
                }
            }
        });
    }

    public void downloadNewGoalsFromServer() {
        String uid = auth.getUid();
        if (uid == null) return;

        long maxUpdatedAt = learningGoalDao.getMaxUpdatedAt();

        // Sử dụng Tasks.await để đợi kết quả từ Firebase (vì đang ở luồng của Worker)
        try {
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task = 
                firestore.collection("users").document(uid)
                    .collection("learning_goals")
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get();
            
            com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots = com.google.android.gms.tasks.Tasks.await(task);
            
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                List<LearningGoalEntity> remoteGoals = queryDocumentSnapshots.toObjects(LearningGoalEntity.class);
                for (LearningGoalEntity remoteGoal : remoteGoals) {
                    remoteGoal.setSyncStatus("synced");
                    learningGoalDao.insertGoal(remoteGoal);
                    Log.d(TAG, "Downloaded goal: " + remoteGoal.getTitle());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải mục tiêu học tập: " + e.getMessage());
        }
    }
}
