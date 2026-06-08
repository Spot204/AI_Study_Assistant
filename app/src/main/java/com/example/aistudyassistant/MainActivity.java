package com.example.aistudyassistant;

import com.example.aistudyassistant.auth.LoginActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
// [ĐÃ SỬA] Import Repository mới thay cho FirestoreService cũ
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [SỬA LỖI] Kiểm tra đăng nhập an toàn hơn (có độ trễ để Firebase kịp đồng bộ)
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        if (mAuth.getCurrentUser() == null && userEmail == null) {
            // Thử đợi thêm một chút hoặc kiểm tra lại
            android.util.Log.d("AUTH_DEBUG", "Đang kiểm tra lại phiên đăng nhập...");
            if (mAuth.getCurrentUser() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);

        // =================================================================
        // 💥 KHỞI ĐỘNG CÁC LUỒNG ĐỒNG BỘ DỮ LIỆU TỰ ĐỘNG (BACKGROUND SYNC)
        // =================================================================

        AppDatabase db = AppDatabase.getDatabase(this);

        // 1. Chạy đồng bộ ngay lập tức cho tất cả repositories
        new UserRepository(db.userDao()).syncUnsyncedUsers();
        new UserStatsRepository(db.userStatsDao()).syncUnsyncedStats();
        new StudySetRepository(db.studySetDao()).syncUnsyncedStudySets();
        new FlashcardRepository(db.flashcardDao()).syncUnsyncedFlashcards();
        new DocumentRepository(db.documentDao()).uploadUnsyncedDocumentsToServer();
        new StudySessionRepository(db.studySessionDao()).syncUnsyncedSessions();
        new QuizRepository(db.quizDao()).syncUnsyncedQuizzes();
        new ScheduleRepository(db.scheduleDao()).syncUnsyncedTasks();
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
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );

        // =================================================================

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Hiển thị màn hình Home mặc định khi ứng dụng khởi động
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_flashcard) {
                selectedFragment = new FlashcardFragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.nav_quiz) {
                selectedFragment = new QuizFragment();
            } else if (itemId == R.id.nav_schedule) {
                selectedFragment = new ScheduleFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}