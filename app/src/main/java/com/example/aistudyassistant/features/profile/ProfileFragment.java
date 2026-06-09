package com.example.aistudyassistant.features.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.services.auth.ProfileService;

public class ProfileFragment extends Fragment {

    private ProfileService profileService;
    private TextView tvName;
    private TextView tvSchool;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_activity_main, container, false);

        profileService = new ProfileService(requireContext());
        tvName = view.findViewById(R.id.tv_profile_name);
        tvSchool = view.findViewById(R.id.tv_profile_school);

        // Header actions
        view.findViewById(R.id.btn_back).setVisibility(View.GONE); // Ẩn nút back khi ở trong Navigation
        view.findViewById(R.id.btn_settings).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show());

        setupMenuItems(view);
        
        // Logout action
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        profileService.getUser(user -> {
            if (user != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvName.setText(user.getFullName());
                    tvSchool.setText(user.getSchool());
                });
            }
        });
    }

    private void setupMenuItems(View view) {
        view.findViewById(R.id.menu_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.menu_notifications).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.menu_goals).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsActivity.LearningGoalsActivity.class);
            startActivity(intent);
        });
        
        view.findViewById(R.id.menu_help).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HelpSupportActivity.class);
            startActivity(intent);
        });
    }
}
