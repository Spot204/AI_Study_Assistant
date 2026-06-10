package com.example.aistudyassistant.data.repository;

import android.content.Context;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.dao.AchievementDao;
import com.example.aistudyassistant.database.dao.UserAchievementDao;
import com.example.aistudyassistant.database.entities.AchievementEntity;
import com.example.aistudyassistant.database.entities.UserAchievementEntity;
import androidx.lifecycle.LiveData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AchievementRepository {
    private final AchievementDao achievementDao;
    private final UserAchievementDao userAchievementDao;
    private final ExecutorService executorService;

    public AchievementRepository(AchievementDao achievementDao, UserAchievementDao userAchievementDao) {
        this.achievementDao = achievementDao;
        this.userAchievementDao = userAchievementDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void seedDefaultAchievements() {
        executorService.execute(() -> {
            List<AchievementEntity> existing = achievementDao.getAllAchievements();
            if (existing.isEmpty()) {
                List<AchievementEntity> defaults = new ArrayList<>();
                
                // Streak Achievements
                defaults.add(new AchievementEntity("ach_streak_3", "Người mới chăm chỉ", "Duy trì học tập liên tục 3 ngày", "ic_flame", "streak", 3));
                defaults.add(new AchievementEntity("ach_streak_7", "Chiến thần kiên trì", "Duy trì học tập liên tục 7 ngày", "ic_flame", "streak", 7));
                
                // Flashcard Achievements
                defaults.add(new AchievementEntity("ach_fc_10", "Người mới bắt đầu", "Hoàn thành 10 flashcard đầu tiên", "ic_book", "flashcards", 10));
                defaults.add(new AchievementEntity("ach_fc_50", "Kho tàng tri thức", "Hoàn thành 50 flashcard", "ic_book", "flashcards", 50));
                
                // Quiz Achievements
                defaults.add(new AchievementEntity("ach_quiz_5", "Quiz Master", "Hoàn thành 5 bài quiz", "ic_trophy", "quiz", 5));
                
                // Study Hours
                defaults.add(new AchievementEntity("ach_hours_5", "Mọt sách chính hiệu", "Đạt 5 giờ học tập", "ic_clock", "hours", 5));

                achievementDao.insertAchievements(defaults);
            }
        });
    }

    public LiveData<List<UserAchievementEntity>> getUserAchievements(String userId) {
        return userAchievementDao.getUserAchievementsLiveData(userId);
    }

    public AchievementEntity getAchievementById(String id) {
        // Note: This is synchronous, should be used carefully
        return achievementDao.getAchievementById(id);
    }
}
