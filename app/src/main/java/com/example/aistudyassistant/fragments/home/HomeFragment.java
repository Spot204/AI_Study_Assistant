package com.example.aistudyassistant.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import com.example.aistudyassistant.fragments.flashcard.FlashcardStudyFragment;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private String userName = "bạn";
    private TextView tvWelcome, tvStreak, tvStudyTime, tvTip;
    private ImageView ivAvatar;
    private RecyclerView rvRecent;
    private RecentStudyAdapter adapter;
    private AppDatabase db;

    private final String[] dailyTips = {
        "Học 25 phút, nghỉ 5 phút (Pomodoro) giúp bạn tập trung hơn đấy!",
        "Giải thích lại kiến thức cho người khác là cách tốt nhất để ghi nhớ.",
        "Đừng quên uống nước và vận động nhẹ sau mỗi giờ học nhé.",
        "Ghi chú bằng sơ đồ tư duy giúp não bộ liên kết thông tin hiệu quả hơn.",
        "Một giấc ngủ ngon giúp củng cố kiến thức bạn đã học trong ngày."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getDatabase(requireContext());
        tvWelcome = view.findViewById(R.id.tvWelcomeHeader);
        tvStreak = view.findViewById(R.id.tvStreakCount);
        tvStudyTime = view.findViewById(R.id.tvStudyTimeDisplay);
        tvTip = view.findViewById(R.id.tvDailyTipText);
        rvRecent = view.findViewById(R.id.rvRecentStudy);
        ivAvatar = view.findViewById(R.id.ivUserAvatar);

        setupRecyclerView();
        displayRandomTip();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RecentStudyAdapter();
        adapter.setOnItemClickListener(studySet -> {
            FlashcardStudyFragment studyFragment = FlashcardStudyFragment.newInstance(
                    studySet.getSetId(),
                    studySet.getTitle()
            );
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, studyFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecent.setAdapter(adapter);
    }

    private void displayRandomTip() {
        int index = new Random().nextInt(dailyTips.length);
        tvTip.setText(dailyTips[index]);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db.userDao().getAnyUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userName = user.getFullName();
                tvWelcome.setText("Chào bạn, " + (userName != null ? userName : "") + "!");
                
                if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatarPath())
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivAvatar);
                }

                loadUserStats(user.getUserId());
                loadStudyTime(user.getUserId());
                loadStudySets(user.getUserId());
            } else {
                tvWelcome.setText("Chào bạn!");
            }
        });
    }

    private void loadUserStats(String userId) {
        new Thread(() -> {
            UserStatsEntity stats = db.userStatsDao().getStatsByUser(userId);
            if (isAdded() && stats != null) {
                requireActivity().runOnUiThread(() -> {
                    tvStreak.setText(stats.getStreakCount() + " ngày");
                });
            }
        }).start();
    }

    private void loadStudyTime(String userId) {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long todayStart = calendar.getTimeInMillis();

            int totalMinutes = db.studySessionDao().getTodayStudyTimeMinutes(userId, todayStart);
            
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvStudyTime.setText(totalMinutes + "/5 phút");
                });
            }
        }).start();
    }

    private void loadStudySets(String userId) {
        new Thread(() -> {
            List<StudySetEntity> sets = db.studySetDao().getStudySetsByUser(userId);
            if (isAdded() && sets != null) {
                requireActivity().runOnUiThread(() -> {
                    // Hiển thị tối đa 5 bộ học tập mới nhất/gần đây
                    int limit = Math.min(sets.size(), 5);
                    adapter.setStudySets(sets.subList(0, limit));
                });
            }
        }).start();
    }
}
