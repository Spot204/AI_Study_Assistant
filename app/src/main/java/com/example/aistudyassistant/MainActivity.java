package com.example.aistudyassistant;

import com.example.aistudyassistant.auth.LoginActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
// [ĐÃ SỬA] Import Repository mới thay cho FirestoreService cũ
import com.example.aistudyassistant.data.repository.StudySessionRepository;

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