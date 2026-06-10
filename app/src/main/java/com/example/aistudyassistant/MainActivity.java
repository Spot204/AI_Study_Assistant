package com.example.aistudyassistant;

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
import com.example.aistudyassistant.data.repository.AchievementRepository;
import com.example.aistudyassistant.services.SyncWorker;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

// ĐƯỜNG DẪN CHUẨN CỦA NHÁNH NAM (Dùng features thay vì fragments)
import com.example.aistudyassistant.features.home.HomeFragment;
import com.example.aistudyassistant.features.chatbot.ChatFragment;
import com.example.aistudyassistant.features.quiz.QuizFragment;
import com.example.aistudyassistant.features.schedule.ScheduleFragment;
import com.example.aistudyassistant.features.profile.ProfileFragment;
import com.example.aistudyassistant.features.flashcard.FlashcardFragment;
import com.example.aistudyassistant.features.auth.LoginFragment;
import com.example.aistudyassistant.features.auth.RegisterFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Kiểm tra đăng nhập
        if (mAuth.getCurrentUser() == null && userEmail == null) {
            loadFragment(new LoginFragment());
            return;
        }

        // Đã đăng nhập -> Chạy đồng bộ toàn tập và load Home
        syncData();

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

    // Tách riêng hàm đồng bộ để gọi lại khi cần thiết (ví dụ sau khi Login xong)
    private void syncData() {
        AppDatabase db = AppDatabase.getDatabase(this);

        // 1. Chạy đồng bộ ngay lập tức cho tất cả repositories
        UserStatsRepository statsRepo = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());

        new AchievementRepository(db.achievementDao(), db.userAchievementDao()).seedDefaultAchievements();
        new UserRepository(db.userDao()).syncUnsyncedUsers();
        statsRepo.syncUnsyncedStats();
        new StudySetRepository(db.studySetDao()).syncUnsyncedStudySets();
        new FlashcardRepository(db.flashcardDao(), statsRepo).syncUnsyncedFlashcards();
        new DocumentRepository(db.documentDao()).uploadUnsyncedDocumentsToServer();
        new StudySessionRepository(db.studySessionDao()).syncUnsyncedSessions();
        new QuizRepository(db.quizDao()).syncUnsyncedQuizzes();
        new ScheduleRepository(this, db.scheduleDao()).syncUnsyncedTasks();
        new LearningGoalRepository(db.learningGoalDao()).syncUnsyncedGoals();

        // 2. Thiết lập WorkManager để tự động đồng bộ định kỳ mỗi 15 phút khi có mạng
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DataSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
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

    // Hàm gọi khi đăng nhập/đăng ký thành công từ các Fragment
    public void navigateToHomeAfterAuth() {
        syncData();
        loadFragment(new HomeFragment());
    }
}