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

    // =================================================================
    // 💥 KHU VỰC KHAI BÁO CÁC DAO (Cổng giao tiếp với SQLite)
    // Quy tắc Room: Phải là hàm abstract, không có ngoặc nhọn {}
    // =================================================================

    public abstract ScheduleDao scheduleDao();
    public abstract UserDao userDao();
    public abstract StudySessionDao studySessionDao(); // Đã sửa đúng tên và kiểu trả về
    public abstract StudySetDao studySetDao();         // Đã xóa ngoặc nhọn lỗi

    // Khai báo thêm các DAO cần thiết cho các Repository chúng ta đã tạo
    public abstract DocumentDao documentDao();
    public abstract FlashcardDao flashcardDao();
    public abstract QuizDao quizDao();

    // =================================================================

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ai_study_assistant_db")
                            // Lệnh này cho phép xóa sạch dữ liệu cũ nếu thay đổi cấu trúc bảng (version tăng lên)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}