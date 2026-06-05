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
import com.example.aistudyassistant.database.AppDatabase;

public class HomeFragment extends Fragment {

    private String userName = "Bá Anh";
    private TextView tvWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcomeHeader);
        TextView tvStreak = view.findViewById(R.id.tvStreakCount);
        TextView tvTip = view.findViewById(R.id.tvDailyTipText);

        tvWelcome.setText("Chào cậu, " + userName + "!");
        tvStreak.setText("7 ngày liên tiếp");
        tvTip.setText("Hãy quét tài liệu hoặc chat trực tiếp với trợ lý AI đề tự do tóm tắt các ghi chú khó nhằn nhé!");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppDatabase.getDatabase(requireContext()).userDao().getAnyUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userName = user.getFullName();
                tvWelcome.setText("Chào cậu, " + userName + "!");
            }
        });
    }
}