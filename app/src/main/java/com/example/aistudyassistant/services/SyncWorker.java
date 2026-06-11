package com.example.aistudyassistant.services;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.aistudyassistant.data.repository.*;
import com.example.aistudyassistant.database.AppDatabase;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Bắt đầu tiến trình đồng bộ ngầm...");
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        
        try {
            // Khởi tạo các repository
            UserRepository userRepo = new UserRepository(db.userDao());
            UserStatsRepository userStatsRepo = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());
            StudySetRepository studySetRepo = new StudySetRepository(db.studySetDao());
            FlashcardRepository flashcardRepo = new FlashcardRepository(db.flashcardDao(), userStatsRepo);
            DocumentRepository documentRepo = new DocumentRepository(db.documentDao());
            StudySessionRepository studySessionRepo = new StudySessionRepository(db.studySessionDao());
            QuizRepository quizRepo = new QuizRepository(db.quizDao(), db.studySetDao());
            ScheduleRepository scheduleRepo = new ScheduleRepository(getApplicationContext(), db.scheduleDao());
            LearningGoalRepository learningGoalRepo = new LearningGoalRepository(db.learningGoalDao());

            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

            // 1. Đồng bộ chiều LÊN (Upload local changes)
            Log.d(TAG, "Đang đẩy dữ liệu local lên server...");
            userRepo.syncUnsyncedUsers();
            userStatsRepo.syncUnsyncedStats();
            studySetRepo.syncUnsyncedStudySets();
            flashcardRepo.syncUnsyncedFlashcards();
            documentRepo.uploadUnsyncedDocumentsToServer();
            studySessionRepo.syncUnsyncedSessions();
            quizRepo.syncUnsyncedQuizzes();
            scheduleRepo.syncUnsyncedTasks();
            learningGoalRepo.syncUnsyncedGoals();
            Log.d(TAG, "Hoàn tất đẩy dữ liệu local.");

            // 2. Đồng bộ chiều XUỐNG (Download remote changes)
            if (currentUid != null) {
                Log.d(TAG, "Bắt đầu tải dữ liệu từ Firebase cho UID: " + currentUid);
                userRepo.downloadUserFromServer(currentUid);
                userStatsRepo.downloadStatsFromServer();
                studySetRepo.downloadNewStudySetsFromServer();
                flashcardRepo.downloadNewFlashcardsFromServer();
                documentRepo.downloadNewDocumentsFromServer(currentUid);
                studySessionRepo.downloadNewSessionsFromServer();
                quizRepo.downloadNewQuizzesFromServer();
                scheduleRepo.downloadNewTasksFromServer();
                learningGoalRepo.downloadNewGoalsFromServer();
                Log.d(TAG, "Hoàn tất yêu cầu tải dữ liệu từ Firebase.");
            } else {
                Log.w(TAG, "Không thể tải dữ liệu: UID người dùng hiện tại là null.");
            }

            Log.i(TAG, "Đồng bộ hoàn tất thành công.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình đồng bộ: " + e.getMessage());
            return Result.retry();
        }
    }
}
