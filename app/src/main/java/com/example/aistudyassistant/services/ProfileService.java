package com.example.aistudyassistant.services;

import android.content.Context;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.features.profile.User;
import com.example.aistudyassistant.features.profile.UserDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileService {
    private final UserDao userDao;
    private final ExecutorService executorService;

    public ProfileService(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
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

    public void getUser(UserCallback callback) {
        executorService.execute(() -> {
            User user = userDao.getUser();
            if (callback != null) {
                callback.onUserLoaded(user);
            }
        });
    }

    public interface UserCallback {
        void onUserLoaded(User user);
    }
}
