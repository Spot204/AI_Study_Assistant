package com.example.aistudyassistant.services.profile;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.dao.UserDao;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.database.entities.UserStatsEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileService {
    private final UserDao userDao;
    private final AppDatabase db;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final ExecutorService executorService;

    public ProfileService(Context context) {
        this.db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.userRepository = new UserRepository(userDao);
        this.userStatsRepository = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void saveUser(User user, Runnable onComplete) {
        userRepository.saveUser(user);
        if (onComplete != null) {
            onComplete.run();
        }
    }

    public void updateUserStats(UserStatsEntity stats) {
        userStatsRepository.updateStats(stats);
    }

    public LiveData<UserStatsEntity> getUserStats(String userId) {
        // Trả về stats từ local (Room)
        return new LiveData<UserStatsEntity>() {
            @Override
            protected void onActive() {
                executorService.execute(() -> postValue(db.userStatsDao().getStatsByUser(userId)));
            }
        };
    }

    public LiveData<User> getCurrentUser() {
        return userDao.getAnyUser();
    }

    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public void logout(Runnable onComplete) {
        executorService.execute(() -> {
            userDao.deleteAll();
            db.studySessionDao().deleteAll();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
