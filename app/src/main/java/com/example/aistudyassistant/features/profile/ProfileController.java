package com.example.aistudyassistant.features.profile;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.database.entities.UserStatsEntity;

public class ProfileController {
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    public ProfileController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.userRepository = new UserRepository(db.userDao());
        this.userStatsRepository = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());
    }

    public LiveData<User> getUser(String userId) {
        return userRepository.getUserById(userId);
    }

    public void updateUserInfo(User user) {
        userRepository.updateUser(user);
    }

    public void updateUserStats(UserStatsEntity stats) {
        userStatsRepository.updateStats(stats);
    }

    public void syncData() {
        userRepository.syncUnsyncedUsers();
        userStatsRepository.syncUnsyncedStats();
    }
}
