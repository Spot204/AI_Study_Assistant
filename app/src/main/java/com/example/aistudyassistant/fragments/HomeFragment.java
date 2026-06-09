package com.example.aistudyassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;

import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.LearningGoalEntity;
import com.example.aistudyassistant.database.entities.QuizEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import com.example.aistudyassistant.features.profile.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private static final String ARG_USER_NAME = "param_user_name";
    private String currentUserId = "test_user_id"; // Replace with actual logged in user ID
    private String userName = "bạn";

    private TextView tvWelcome, tvStreak, tvStudyTime, tvGoalProgress, tvDailyTip;
    private ProgressBar pbGoal;
    private RecyclerView rvRecent;
    private RecentStudyAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LearningGoalEntity currentGoal;

    private final String[] tips = {
        "Hãy quét tài liệu hoặc chat trực tiếp với trợ lý AI để tự do tóm tắt các ghi chú khó nhằn nhé!",
        "Học tập ngắt quãng (Spaced Repetition) giúp bạn nhớ lâu hơn gấp 3 lần.",
        "Đừng quên nghỉ ngơi 5 phút sau mỗi 25 phút học tập (kỹ thuật Pomodoro).",
        "Giải thích kiến thức cho người khác là cách tốt nhất để chính mình hiểu sâu hơn.",
        "Uống đủ nước và giữ tư thế ngồi thẳng giúp tăng sự tập trung."
    };

    public static HomeFragment newInstance(String name) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(ARG_USER_NAME);
        }
        db = AppDatabase.getDatabase(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        loadData();
        displayRandomTip();

        view.findViewById(R.id.btnChangeGoal).setOnClickListener(v -> showChangeGoalDialog());

        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcomeHeader);
        tvStreak = view.findViewById(R.id.tvStreakCount);
        tvStudyTime = view.findViewById(R.id.tvStudyTimeDisplay);
        tvGoalProgress = view.findViewById(R.id.tvGoalProgressText);
        tvDailyTip = view.findViewById(R.id.tvDailyTipText);
        pbGoal = view.findViewById(R.id.pbGoalProgress);
        rvRecent = view.findViewById(R.id.rvRecentStudy);
    }

    private void setupRecyclerView() {
        adapter = new RecentStudyAdapter();
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecent.setAdapter(adapter);
    }

    private void displayRandomTip() {
        int index = new Random().nextInt(tips.length);
        tvDailyTip.setText(tips[index]);
    }

    private void showChangeGoalDialog() {
        if (currentGoal == null) {
            Toast.makeText(getContext(), "Không tìm thấy mục tiêu để thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thay đổi mục tiêu hàng ngày");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf((int)currentGoal.getTargetValue()));
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                double newTarget = Double.parseDouble(val);
                updateGoal(newTarget);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, id) -> dialog.cancel());
        builder.show();
    }

    private void updateGoal(double newTarget) {
        executor.execute(() -> {
            currentGoal.setTargetValue(newTarget);
            db.learningGoalDao().updateGoal(currentGoal);
            loadData(); // Reload UI
        });
    }

    private void loadData() {
        executor.execute(() -> {
            // Get user info
            User user = db.userDao().getUserById(currentUserId);
            UserStatsEntity stats = db.userStatsDao().getStatsByUser(currentUserId);
            List<LearningGoalEntity> goals = db.learningGoalDao().getGoalsByUser(currentUserId);
            List<StudySessionEntity> sessions = db.studySessionDao().getSessionsByUser(currentUserId);

            // Fetch titles for recent activities
            List<RecentStudyAdapter.RecentActivity> displayActivities = new ArrayList<>();
            if (sessions != null) {
                int limit = Math.min(sessions.size(), 5);
                for (int i = 0; i < limit; i++) {
                    StudySessionEntity session = sessions.get(i);
                    String title = "Hoạt động học tập";
                    
                    if ("quiz".equalsIgnoreCase(session.getType())) {
                        QuizEntity q = db.quizDao().getQuizById(session.getReferenceId());
                        if (q != null) title = q.getTitle();
                    } else if ("flashcard".equalsIgnoreCase(session.getType())) {
                        StudySetEntity s = db.studySetDao().getSetById(session.getReferenceId());
                        if (s != null) title = s.getTitle();
                    }
                    displayActivities.add(new RecentStudyAdapter.RecentActivity(session, title));
                }
            }

            // Calculate study time today
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfToday = cal.getTimeInMillis();
            int todayMinutes = db.studySessionDao().getTotalDurationSince(currentUserId, startOfToday);

            mainHandler.post(() -> {
                if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                    tvWelcome.setText("Chào bạn, " + user.getFullName() + "!");
                } else {
                    tvWelcome.setText("Chào bạn!");
                }

                if (stats != null) {
                    tvStreak.setText(stats.getStreakCount() + " ngày");
                }
                tvStudyTime.setText(todayMinutes + " phút");

                if (goals != null && !goals.isEmpty()) {
                    currentGoal = goals.get(0);
                    int progress = (int) ((currentGoal.getCurrentValue() / currentGoal.getTargetValue()) * 100);
                    pbGoal.setProgress(Math.min(progress, 100));
                    tvGoalProgress.setText((int)currentGoal.getCurrentValue() + " / " + (int)currentGoal.getTargetValue() + " " + (currentGoal.getUnit() != null ? currentGoal.getUnit() : "phút") + " đã hoàn thành");
                }

                adapter.setActivities(displayActivities);
            });
        });
    }
}
