package com.example.aistudyassistant;

import android.util.Log;
import com.example.aistudyassistant.features.auth.LoginActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.models.Achievement;
import com.example.aistudyassistant.models.Document;
import com.example.aistudyassistant.models.Flashcard;
import com.example.aistudyassistant.models.LearningGoal;
import com.example.aistudyassistant.models.Notification;
import com.example.aistudyassistant.models.Profile;
import com.example.aistudyassistant.models.Question;
import com.example.aistudyassistant.models.Quiz;
import com.example.aistudyassistant.models.Session;
import com.example.aistudyassistant.models.StudySet;
import com.example.aistudyassistant.models.User;
import com.example.aistudyassistant.models.UserAchievement;
import com.example.aistudyassistant.models.UserStats;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.*;
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

        // Seed data to Firebase (Uncommented as requested to run once)
        seedDataToFirebase();

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

    private void seedDataToFirebase() {
        Log.d("SEED", "Starting data seeding...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String testUserId = "test_user_123";

        // 1. Seed User
        com.example.aistudyassistant.models.User user = new com.example.aistudyassistant.models.User(testUserId, "user@example.com", "TestUser", "https://example.com/photo.jpg");
        db.collection("users").document(testUserId).set(user)
                .addOnSuccessListener(aVoid -> Log.d("SEED", "User seeded successfully"))
                .addOnFailureListener(e -> Log.e("SEED", "Error seeding user", e));

        // 2. Seed Profile
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_xp", 1500);
        stats.put("level", 5);
        stats.put("current_streak", 3);

        Map<String, Object> settings = new HashMap<>();
        settings.put("theme", "dark");
        settings.put("reminder_time", "08:00");

        com.example.aistudyassistant.models.Profile profile = new com.example.aistudyassistant.models.Profile(testUserId, "Nguyen Van A", "https://example.com/avatar.jpg", "Bio here", false, Timestamp.now(), "free", stats, settings);
        db.collection("profiles").document(testUserId).set(profile)
                .addOnSuccessListener(aVoid -> Log.d("SEED", "Profile seeded successfully"));

        // 3. Seed Achievements
        com.example.aistudyassistant.models.Achievement a1 = new com.example.aistudyassistant.models.Achievement("streak_7", "7 Day Streak", "Study for 7 days in a row", "https://example.com/icon1.png", "streak", 7);
        com.example.aistudyassistant.models.Achievement a2 = new com.example.aistudyassistant.models.Achievement("quiz_10", "Quiz Master", "Complete 10 quizzes", "https://example.com/icon2.png", "quiz", 10);
        db.collection("achievements").document(a1.getAchievementId()).set(a1);
        db.collection("achievements").document(a2.getAchievementId()).set(a2);

        // 4. Seed User Achievements
        com.example.aistudyassistant.models.UserAchievement ua1 = new com.example.aistudyassistant.models.UserAchievement(testUserId + "_streak_7", testUserId, "streak_7", Timestamp.now());
        db.collection("user_achievements").document(ua1.getId()).set(ua1);

        // 5. Seed User Stats
        com.example.aistudyassistant.models.UserStats userStats = new com.example.aistudyassistant.models.UserStats(testUserId, 3, 50, 12, 15.5, Timestamp.now());
        db.collection("user_stats").document(testUserId).set(userStats);

        // 6. Seed Learning Goals
        com.example.aistudyassistant.models.LearningGoal goal = new com.example.aistudyassistant.models.LearningGoal("goal_1", testUserId, "Learn Java", 5, 10, "h", Timestamp.now(), "active");
        db.collection("learning_goals").document(goal.getGoalId()).set(goal);

        // 7. Seed Notifications
        com.example.aistudyassistant.models.Notification note = new com.example.aistudyassistant.models.Notification("note_1", testUserId, "Welcome!", "Thanks for joining Ai Study Assistant", "system", false, Timestamp.now());
        db.collection("notifications").document(note.getNotificationId()).set(note);

        // 8. Seed Study Sets
        com.example.aistudyassistant.models.StudySet set = new com.example.aistudyassistant.models.StudySet("set_1", testUserId, "Basic Java", "Public", 5, Timestamp.now());
        db.collection("study_sets").document(set.getSetId()).set(set);

        // 9. Seed Flashcards
        com.example.aistudyassistant.models.Flashcard card = new com.example.aistudyassistant.models.Flashcard("card_1", "set_1", "Variable", "A container for data", "", false, Timestamp.now());
        db.collection("flashcards").document(card.getCardId()).set(card);

        // 10. Seed Documents
        com.example.aistudyassistant.models.Document doc = new com.example.aistudyassistant.models.Document("doc_1", testUserId, "Java Basics PDF", "https://example.com/java.pdf", "Summary of Java", Timestamp.now());
        db.collection("documents").document(doc.getDocumentId()).set(doc);

        // 11. Seed Quizzes
        com.example.aistudyassistant.models.Quiz quiz = new com.example.aistudyassistant.models.Quiz("quiz_1", testUserId, "Java Intro Quiz", 80, 10, Timestamp.now());
        db.collection("quizzes").document(quiz.getQuizId()).set(quiz);

        // 12. Seed Questions
        List<String> options = Arrays.asList("Option A", "Option B", "Option C", "Option D");
        com.example.aistudyassistant.models.Question q1 = new com.example.aistudyassistant.models.Question("q_1", "quiz_1", "What is Java?", options, 0);
        db.collection("questions").document(q1.getQuestionId()).set(q1);

        // 13. Seed Sessions
        com.example.aistudyassistant.models.Session session = new com.example.aistudyassistant.models.Session("sess_1", testUserId, "set_1", Timestamp.now(), Timestamp.now(), 30, "study");
        db.collection("sessions").document(session.getSessionId()).set(session);
        
        Log.d("SEED", "All seed tasks sent to Firestore.");
    }
}