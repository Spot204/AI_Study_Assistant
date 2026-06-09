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

    public UserStatsRepository(UserStatsDao userStatsDao) {
        this.userStatsDao = userStatsDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Cập nhật toàn bộ object stats
     */
    public void updateStats(UserStatsEntity stats) {
        stats.setUpdatedAt(System.currentTimeMillis());
        stats.setSyncStatus("pending_update");

        executorService.execute(() -> {
            userStatsDao.updateStats(stats);
            syncToCloud(stats);
        });
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
