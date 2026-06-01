package com.example.aistudyassistant.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.aistudyassistant.features.profile.User;
import com.example.aistudyassistant.features.profile.UserDao;
import com.example.aistudyassistant.features.schedule.ScheduleDao;
import com.example.aistudyassistant.features.schedule.ScheduleTask;

@Database(entities = {ScheduleTask.class, User.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ScheduleDao scheduleDao();
    public abstract UserDao userDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ai_study_assistant_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
