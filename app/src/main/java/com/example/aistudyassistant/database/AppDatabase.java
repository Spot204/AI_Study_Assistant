package com.example.aistudyassistant.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.aistudyassistant.database.converters.Converters;
import com.example.aistudyassistant.database.dao.*;
import com.example.aistudyassistant.database.entities.*;

@Database(entities = {
        ScheduleTask.class, 
        User.class, 
        StudySessionEntity.class,
        StudySetEntity.class,
        FlashcardEntity.class,
        UserStatsEntity.class,
        LearningGoalEntity.class,
        AchievementEntity.class,
        UserAchievementEntity.class,
        NotificationEntity.class,
        QuizEntity.class,
        DocumentEntity.class
}, version = 4)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // Khai báo các DAO (Sử dụng các class trong package database.dao)
    public abstract ScheduleDao scheduleDao();
    public abstract UserDao userDao();
    public abstract StudySessionDao studySessionDao();
    public abstract StudySetDao studySetDao();
    public abstract FlashcardDao flashcardDao();
    public abstract UserStatsDao userStatsDao();
    public abstract LearningGoalDao learningGoalDao();
    public abstract AchievementDao achievementDao();
    public abstract UserAchievementDao userAchievementDao();
    public abstract NotificationDao notificationDao();
    public abstract QuizDao quizDao();
    public abstract DocumentDao documentDao();

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
