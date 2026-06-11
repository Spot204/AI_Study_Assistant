package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.UserStatsDao;
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserStatsRepository {
    private static final String TAG = "UserStatsRepository";
    private final UserStatsDao userStatsDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    private final com.example.aistudyassistant.database.dao.AchievementDao achievementDao;
    private final com.example.aistudyassistant.database.dao.UserAchievementDao userAchievementDao;

    public UserStatsRepository(UserStatsDao userStatsDao, 
                               com.example.aistudyassistant.database.dao.AchievementDao achievementDao,
                               com.example.aistudyassistant.database.dao.UserAchievementDao userAchievementDao) {
        this.userStatsDao = userStatsDao;
        this.achievementDao = achievementDao;
        this.userAchievementDao = userAchievementDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Cập nhật toàn bộ object stats và kiểm tra danh hiệu
     */
    public void updateStats(UserStatsEntity stats) {
        stats.setUpdatedAt(System.currentTimeMillis());
        stats.setSyncStatus("pending_update");

        executorService.execute(() -> {
            userStatsDao.insertStats(stats);
            checkAchievements(stats); // KIỂM TRA MỞ KHÓA DANH HIỆU
            syncToCloud(stats);
        });
    }

    private void checkAchievements(UserStatsEntity stats) {
        List<com.example.aistudyassistant.database.entities.AchievementEntity> allAchievements = achievementDao.getAllAchievements();
        for (com.example.aistudyassistant.database.entities.AchievementEntity achievement : allAchievements) {
            String userAchId = stats.getUserId() + "_" + achievement.getAchievementId();
            
            // Nếu chưa mở khóa, hãy kiểm tra điều kiện
            if (!userAchievementDao.isUnlocked(userAchId)) {
                boolean reached = false;
                switch (achievement.getRequirementType()) {
                    case "streak": reached = stats.getStreakCount() >= achievement.getRequirementValue(); break;
                    case "quiz": reached = stats.getTotalQuizzes() >= achievement.getRequirementValue(); break;
                    case "hours": reached = stats.getStudyHours() >= achievement.getRequirementValue(); break;
                    case "flashcards": reached = stats.getTotalFlashcards() >= achievement.getRequirementValue(); break;
                }

                if (reached) {
                    com.example.aistudyassistant.database.entities.UserAchievementEntity newUnlock = 
                        new com.example.aistudyassistant.database.entities.UserAchievementEntity(userAchId, stats.getUserId(), achievement.getAchievementId());
                    userAchievementDao.unlockAchievement(newUnlock);
                    // Đồng bộ danh hiệu lên cloud (có thể gọi repo achievement ở đây)
                }
            }
        }
    }

    /**
     * Cộng thêm số lượng Flashcards
     */
    public void incrementFlashcards(String userId, int amount) {
        executorService.execute(() -> {
            UserStatsEntity stats = userStatsDao.getStatsByUser(userId);
            if (stats == null) stats = new UserStatsEntity(userId);
            
            stats.setTotalFlashcards(stats.getTotalFlashcards() + amount);
            updateStats(stats);
        });
    }

    /**
     * Cộng thêm số lượng Quiz đã hoàn thành
     */
    public void incrementQuizzes(String userId) {
        executorService.execute(() -> {
            UserStatsEntity stats = userStatsDao.getStatsByUser(userId);
            if (stats == null) stats = new UserStatsEntity(userId);
            
            stats.setTotalQuizzes(stats.getTotalQuizzes() + 1);
            updateStats(stats);
        });
    }

    /**
     * Cộng thêm số giờ học
     */
    public void addStudyHours(String userId, double hours) {
        executorService.execute(() -> {
            UserStatsEntity stats = userStatsDao.getStatsByUser(userId);
            if (stats == null) stats = new UserStatsEntity(userId);
            
            stats.setStudyHours(stats.getStudyHours() + hours);
            updateStats(stats);
        });
    }

    /**
     * Cập nhật số ngày học và Streak (Gọi mỗi khi user mở app hoặc hoàn thành 1 bài học)
     */
    public void updateStudyDays(String userId) {
        executorService.execute(() -> {
            UserStatsEntity stats = userStatsDao.getStatsByUser(userId);
            if (stats == null) stats = new UserStatsEntity(userId);

            long currentTime = System.currentTimeMillis();
            long lastActive = stats.getLastActive();

            if (!isSameDay(currentTime, lastActive)) {
                stats.setTotalStudyDays(stats.getTotalStudyDays() + 1);
                
                // Kiểm tra nếu là ngày tiếp theo thì tăng streak, nếu cách quãng thì reset
                if (isNextDay(currentTime, lastActive)) {
                    stats.setStreakCount(stats.getStreakCount() + 1);
                } else {
                    stats.setStreakCount(1);
                }
            }
            
            stats.setLastActive(currentTime);
            updateStats(stats);
        });
    }

    private boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isNextDay(long currentTime, long lastTime) {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastTime);
        lastCal.add(Calendar.DAY_OF_YEAR, 1);
        
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentTime);
        
        return lastCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
               lastCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR);
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
        String uid = auth.getUid();
        if (uid == null) return;

        try {
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot> task = 
                firestore.collection("users").document(uid)
                    .collection("stats").document("current")
                    .get();
            
            com.google.firebase.firestore.DocumentSnapshot documentSnapshot = com.google.android.gms.tasks.Tasks.await(task);
            
            if (documentSnapshot.exists()) {
                UserStatsEntity remoteStats = documentSnapshot.toObject(UserStatsEntity.class);
                if (remoteStats != null) {
                    remoteStats.setSyncStatus("synced");
                    userStatsDao.insertStats(remoteStats);
                    Log.d(TAG, "Downloaded stats for user: " + uid);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải chỉ số người dùng: " + e.getMessage());
        }
    }
}
