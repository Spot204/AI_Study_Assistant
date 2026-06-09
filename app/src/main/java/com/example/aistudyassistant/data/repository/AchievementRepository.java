package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.AchievementDao;
import com.example.aistudyassistant.database.dao.UserAchievementDao;
import com.example.aistudyassistant.database.entities.AchievementEntity;
import com.example.aistudyassistant.database.entities.UserAchievementEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AchievementRepository {
    private static final String TAG = "AchievementRepository";
    private final AchievementDao achievementDao;
    private final UserAchievementDao userAchievementDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public AchievementRepository(AchievementDao achievementDao, UserAchievementDao userAchievementDao) {
        this.achievementDao = achievementDao;
        this.userAchievementDao = userAchievementDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Lấy danh sách tất cả các loại danh hiệu hiện có trong hệ thống
     */
    public List<AchievementEntity> getAllAchievements() {
        return achievementDao.getAllAchievements();
    }

    /**
     * Mở khóa một danh hiệu cho người dùng
     */
    public void unlockAchievement(String achievementId) {
        String uid = auth.getUid();
        if (uid == null) return;

        String id = uid + "_" + achievementId;
        executorService.execute(() -> {
            if (!userAchievementDao.isUnlocked(id)) {
                UserAchievementEntity userAchievement = new UserAchievementEntity(id, uid, achievementId);
                userAchievementDao.unlockAchievement(userAchievement);
                syncAchievementToCloud(userAchievement);
            }
        });
    }

    /**
     * Đồng bộ một danh hiệu vừa mở khóa lên Cloud
     */
    private void syncAchievementToCloud(UserAchievementEntity userAchievement) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("achievements").document(userAchievement.getAchievementId())
                .set(userAchievement)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    userAchievement.setSyncStatus("synced");
                    userAchievementDao.unlockAchievement(userAchievement);
                    Log.d(TAG, "Achievement synced to cloud: " + userAchievement.getAchievementId());
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Achievement sync failed", e));
    }

    /**
     * HÀM UPLOAD: Đẩy các danh hiệu đã đạt được offline lên Firebase
     */
    public void uploadUnsyncedAchievements() {
        executorService.execute(() -> {
            String uid = auth.getUid();
            if (uid == null) return;

            List<UserAchievementEntity> unsynced = userAchievementDao.getUnsyncedUserAchievements();
            if (unsynced == null || unsynced.isEmpty()) return;

            WriteBatch batch = firestore.batch();
            for (UserAchievementEntity ua : unsynced) {
                batch.set(firestore.collection("users").document(uid)
                        .collection("achievements").document(ua.getAchievementId()), ua);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                for (UserAchievementEntity ua : unsynced) {
                    ua.setSyncStatus("synced");
                    userAchievementDao.unlockAchievement(ua);
                }
                Log.d(TAG, "All unsynced achievements uploaded");
            }));
        });
    }

    /**
     * HÀM DOWNLOAD: Tải các danh hiệu đã đạt được từ server về máy
     */
    public void downloadAchievementsFromServer() {
        executorService.execute(() -> {
            String uid = auth.getUid();
            if (uid == null) return;

            firestore.collection("users").document(uid)
                    .collection("achievements")
                    .get()
                    .addOnSuccessListener(querySnapshot -> executorService.execute(() -> {
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<UserAchievementEntity> remoteAchievements = querySnapshot.toObjects(UserAchievementEntity.class);
                            for (UserAchievementEntity ua : remoteAchievements) {
                                ua.setSyncStatus("synced");
                                userAchievementDao.unlockAchievement(ua);
                            }
                            Log.d(TAG, "Achievements downloaded from server");
                        }
                    }));
        });
    }
}
