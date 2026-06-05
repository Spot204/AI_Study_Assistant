package com.example.aistudyassistant;

import com.example.aistudyassistant.features.auth.LoginActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
// [ĐÃ SỬA] Import Repository mới thay cho FirestoreService cũ
import com.example.aistudyassistant.data.repository.StudySessionRepository;

import com.example.aistudyassistant.features.chatbot.ChatFragment;
import com.example.aistudyassistant.features.profile.ProfileFragment;
import com.example.aistudyassistant.features.quiz.QuizFragment;
import com.example.aistudyassistant.features.schedule.ScheduleFragment;
import com.example.aistudyassistant.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra xem user đã đăng nhập chưa (Bỏ qua nếu là tài khoản admin test)
        boolean isMockLogin = getIntent().getStringExtra("USER_EMAIL") != null;
        if (FirebaseAuth.getInstance().getCurrentUser() == null && !isMockLogin) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // =================================================================
        // 💥 KHỞI ĐỘNG CÁC LUỒNG ĐỒNG BỘ DỮ LIỆU TỰ ĐỘNG (BACKGROUND SYNC)
        // =================================================================

        // Khởi tạo Repository và gọi hàm đồng bộ các phiên học (Study Sessions) chưa được đẩy lên mạng
        StudySessionRepository sessionRepo = new StudySessionRepository(
                AppDatabase.getDatabase(this).studySessionDao()
        );
        sessionRepo.syncUnsyncedSessions();

        /* 💡 GỢI Ý MỞ RỘNG:
           Sau này, nếu bạn muốn app tự động đồng bộ cả Lịch trình (Schedule) và Tài liệu (Document)
           ngay khi mở app, bạn chỉ cần gọi thêm các Repository tương ứng ở đây. Ví dụ:

           ScheduleRepository scheduleRepo = new ScheduleRepository(AppDatabase.getDatabase(this).scheduleDao());
           scheduleRepo.uploadUnsyncedTasksToServer();
        */

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