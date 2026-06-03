package com.example.aistudyassistant.services.auth;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.dao.UserDao;
import com.example.aistudyassistant.database.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileService {
    private final UserDao userDao;
    private final AppDatabase db;
    private final ExecutorService executorService;

    public ProfileService(Context context) {
        this.db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void saveUser(User user, Runnable onComplete) {
        executorService.execute(() -> {
            userDao.insertUser(user);
            if (onComplete != null) {
                onComplete.run();
            }
        });
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
