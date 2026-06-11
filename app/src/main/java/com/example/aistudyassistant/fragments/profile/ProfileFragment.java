package com.example.aistudyassistant.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.fragments.auth.LoginFragment;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.services.auth.AuthService;
import com.example.aistudyassistant.services.profile.ProfileService;
import com.example.aistudyassistant.data.repository.AchievementRepository;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ProfileService profileService;
    private UserStatsRepository statsRepo;
    private AchievementRepository achievementRepo;
    private AchievementAdapter achievementAdapter;
    private TextView tvName, tvSchool;
    private TextView tvStreak, tvFlashcards, tvQuizzes, tvHours;
    private ImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_activity_main, container, false);

        profileService = new ProfileService(requireContext());
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        statsRepo = new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao());
        achievementRepo = new AchievementRepository(db.achievementDao(), db.userAchievementDao());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvSchool = view.findViewById(R.id.tv_profile_school);
        ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvStreak = view.findViewById(R.id.tv_stat_streak);
        tvFlashcards = view.findViewById(R.id.tv_stat_flashcards);
        tvQuizzes = view.findViewById(R.id.tv_stat_quizzes);
        tvHours = view.findViewById(R.id.tv_stat_hours);

        RecyclerView rvAchievements = view.findViewById(R.id.achievements_recycler);
        achievementAdapter = new AchievementAdapter();
        rvAchievements.setAdapter(achievementAdapter);

        // Header actions
        view.findViewById(R.id.btn_back).setVisibility(View.GONE); // Ẩn nút back khi ở trong Navigation
        setupMenuItems(view);
        
        // Logout action
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AuthService().Logout();
            profileService.logout(() -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof com.example.aistudyassistant.MainActivity) {
                            ((com.example.aistudyassistant.MainActivity) getActivity()).loadFragment(new LoginFragment());
                        }
                    });
                }
            });
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserProfile();
    }

    private void loadUserProfile() {
        profileService.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvName.setText(user.getFullName());
                tvSchool.setText(user.getSchool());
                
                if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatarPath())
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(ivAvatar);
                }

                loadUserStats(user.getUserId());
                loadUserAchievements(user.getUserId());
            }
        });
    }

    private void loadUserAchievements(String userId) {
        achievementRepo.getUserAchievements(userId).observe(getViewLifecycleOwner(), achievements -> {
            if (achievements != null) {
                achievementAdapter.setAchievements(achievements);
            }
        });
    }

    private void loadUserStats(String userId) {
        // Giả sử statsRepo có phương thức trả về LiveData hoặc chúng ta lấy trực tiếp từ DAO qua Repository
        // Ở đây tôi sẽ thêm logic lấy data từ Local DB để hiển thị
        AppDatabase.getDatabase(requireContext()).userStatsDao().getStatsLiveData(userId).observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                tvStreak.setText(String.valueOf(stats.getStreakCount()));
                tvFlashcards.setText(String.valueOf(stats.getTotalFlashcards()));
                tvQuizzes.setText(String.valueOf(stats.getTotalQuizzes()));
                tvHours.setText(String.format(Locale.getDefault(), "%.1f", stats.getStudyHours()));
            }
        });
    }

    private void setupMenuItems(View view) {
        view.findViewById(R.id.menu_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
        view.findViewById(R.id.menu_help).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HelpSupportActivity.class);
            startActivity(intent);
        });
    }
}
