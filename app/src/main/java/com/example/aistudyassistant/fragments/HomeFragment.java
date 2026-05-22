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

public class HomeFragment extends Fragment {

    private static final String ARG_USER_NAME = "param_user_name";
    private String userName = "Bá Anh";

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvWelcome = view.findViewById(R.id.tvWelcomeHeader);
        TextView tvStreak = view.findViewById(R.id.tvStreakCount);
        TextView tvTip = view.findViewById(R.id.tvDailyTipText);

        tvWelcome.setText("Chào cậu, " + userName + "!");
        tvStreak.setText("7 ngày liên tiếp");
        tvTip.setText("Hãy quét tài liệu hoặc chat trực tiếp với trợ lý AI đề tự do tóm tắt các ghi chú khó nhằn nhé!");

        return view;
    }
}