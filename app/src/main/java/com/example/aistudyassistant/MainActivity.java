package com.example.aistudyassistant;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.data.repository.StudySessionRepository;
import com.example.aistudyassistant.fragments.home.HomeFragment;
import com.example.aistudyassistant.fragments.chatbot.ChatFragment;
import com.example.aistudyassistant.fragments.quiz.QuizFragment;
import com.example.aistudyassistant.fragments.schedule.ScheduleFragment;
import com.example.aistudyassistant.fragments.profile.ProfileFragment;
import com.example.aistudyassistant.fragments.flashcard.FlashcardFragment;
import com.example.aistudyassistant.fragments.auth.LoginFragment;
import com.example.aistudyassistant.fragments.auth.RegisterFragment; // Cần import để check ẩn/hiện
import com.example.aistudyassistant.services.SyncWorker;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Kiểm tra đăng nhập
        if (mAuth.getCurrentUser() == null) {
            // Chưa đăng nhập -> Load LoginFragment
            loadFragment(new LoginFragment());
        } else {
            // Đã đăng nhập -> Load HomeFragment và chạy đồng bộ
            syncData();

            if (savedInstanceState == null) {
                loadFragment(new HomeFragment());
            }
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
        // Nếu là trang Auth thì ẩn thanh điều hướng
        if (fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void syncData() {
        // 1. Chạy đồng bộ ngay lập tức cho các phiên học chưa lưu
        StudySessionRepository sessionRepo = new StudySessionRepository(
                AppDatabase.getDatabase(this).studySessionDao()
        );
        sessionRepo.syncUnsyncedSessions();

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
}