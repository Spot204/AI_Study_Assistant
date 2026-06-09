package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.UserStatsDao;
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserStatsRepository {
    private static final String TAG = "UserStatsRepository";
    private final UserStatsDao userStatsDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public UserStatsRepository(UserStatsDao userStatsDao) {
        this.userStatsDao = userStatsDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void updateStats(UserStatsEntity stats) {
        stats.setUpdatedAt(System.currentTimeMillis());
        stats.setSyncStatus("pending_update");

        executorService.execute(() -> {
            userStatsDao.updateStats(stats);
            syncToCloud(stats);
        });
    }

    private void syncToCloud(UserStatsEntity stats) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("stats").document("current")
                .set(stats)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    stats.setSyncStatus("synced");
                    userStatsDao.updateStats(stats);
                    Log.d(TAG, "UserStats synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "UserStats sync failed", e));
    }

    public void syncUnsyncedStats() {
        executorService.execute(() -> {
            List<UserStatsEntity> unsynced = userStatsDao.getUnsyncedUserStats();
            if (unsynced != null && !unsynced.isEmpty()) {
                for (UserStatsEntity stats : unsynced) {
                    syncToCloud(stats);
                }
            }
        });
    }

    public void downloadStatsFromServer() {
        executorService.execute(() -> {
            String uid = auth.getUid();
            if (uid == null) return;

            firestore.collection("users").document(uid)
                    .collection("stats").document("current")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserStatsEntity remoteStats = documentSnapshot.toObject(UserStatsEntity.class);
                            if (remoteStats != null) {
                                executorService.execute(() -> {
                                    remoteStats.setSyncStatus("synced");
                                    userStatsDao.insertStats(remoteStats);
                                });
                            }
                        }
                    });
        });
    }
}
