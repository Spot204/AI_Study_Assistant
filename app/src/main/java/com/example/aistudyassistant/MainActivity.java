package com.example.aistudyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.data.repository.StudySessionRepository;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.DocumentRepository;
import com.example.aistudyassistant.data.repository.QuizRepository;
import com.example.aistudyassistant.data.repository.ScheduleRepository;
import com.example.aistudyassistant.data.repository.LearningGoalRepository;
import com.example.aistudyassistant.services.SyncWorker;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import com.example.aistudyassistant.fragments.home.HomeFragment;
import com.example.aistudyassistant.fragments.chatbot.ChatFragment;
import com.example.aistudyassistant.fragments.quiz.QuizFragment;
import com.example.aistudyassistant.fragments.schedule.ScheduleFragment;
import com.example.aistudyassistant.fragments.profile.ProfileFragment;
import com.example.aistudyassistant.fragments.flashcard.FlashcardFragment;
import com.example.aistudyassistant.fragments.auth.LoginFragment;
import com.example.aistudyassistant.fragments.auth.RegisterFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.work.ExistingPeriodicWorkPolicy;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        if (mAuth.getCurrentUser() == null && userEmail == null) {
            loadFragment(new LoginFragment());
            return;
        }

        // =================================================================
        // 💥 KHỞI ĐỘNG CÁC LUỒNG ĐỒNG BỘ DỮ LIỆU TỰ ĐỘNG (BACKGROUND SYNC)
        // =================================================================

        AppDatabase db = AppDatabase.getDatabase(this);

        // 1. Chạy đồng bộ ngay lập tức cho tất cả repositories
        new UserRepository(db.userDao()).syncUnsyncedUsers();
        UserStatsRepository statsRepo = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());
        statsRepo.syncUnsyncedStats();
        
        // Seed achievements
        new com.example.aistudyassistant.data.repository.AchievementRepository(db.achievementDao(), db.userAchievementDao()).seedDefaultAchievements();

        new UserRepository(db.userDao()).syncUnsyncedUsers();
        new StudySetRepository(db.studySetDao()).syncUnsyncedStudySets();
        new FlashcardRepository(db.flashcardDao(), statsRepo).syncUnsyncedFlashcards();
        new DocumentRepository(db.documentDao()).uploadUnsyncedDocumentsToServer();
        new StudySessionRepository(db.studySessionDao()).syncUnsyncedSessions();
        new QuizRepository(db.quizDao()).syncUnsyncedQuizzes();
        new ScheduleRepository(this, db.scheduleDao()).syncUnsyncedTasks();
        new LearningGoalRepository(db.learningGoalDao()).syncUnsyncedGoals();

        // 2. Thiết lập WorkManager để đồng bộ định kỳ mỗi 15 phút
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CloudSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );

        // =================================================================

        // Hiển thị màn hình Home mặc định khi ứng dụng khởi động
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) selectedFragment = new HomeFragment();
            else if (itemId == R.id.nav_flashcard) selectedFragment = new FlashcardFragment();
            else if (itemId == R.id.nav_chat) selectedFragment = new ChatFragment();
            else if (itemId == R.id.nav_quiz) selectedFragment = new QuizFragment();
            else if (itemId == R.id.nav_schedule) selectedFragment = new ScheduleFragment();
            else if (itemId == R.id.nav_profile) selectedFragment = new ProfileFragment();

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    // Hàm loadFragment thông minh: tự ẩn/hiện BottomNav
    public void loadFragment(Fragment fragment) {
        if (bottomNav == null) {
            bottomNav = findViewById(R.id.bottomNavigation);
        }

        // Nếu là trang Auth thì ẩn thanh điều hướng
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        } else {
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
