package com.example.aistudyassistant.services.profile;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.dao.UserDao;
import com.example.aistudyassistant.database.entities.User;

public class ProfileService {
    private final UserDao userDao;
    private final AppDatabase db;
    private final UserRepository userRepository;

    public ProfileService(Context context) {
        this.db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.userRepository = new UserRepository(userDao);
    }

    /**
     * Lưu User đồng thời đồng bộ lên Firestore
     */
    public void saveUser(User user, Runnable onComplete) {
        userRepository.saveUser(user);
        if (onComplete != null) {
            onComplete.run();
        }
    }

    public LiveData<User> getCurrentUser() {
        return userDao.getAnyUser();
    }

    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public void logout(Runnable onComplete) {
        new java.util.concurrent.ScheduledThreadPoolExecutor(1).execute(() -> {
            userDao.deleteAll();
            db.studySessionDao().deleteAll();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
