package com.example.aistudyassistant.features.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aistudyassistant.database.AppDatabase;
import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ScheduleFragment extends Fragment {

    private RecyclerView rvTasks;
    private ScheduleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule, container, false);

        rvTasks = view.findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new ScheduleAdapter(new ArrayList<>());
        rvTasks.setAdapter(adapter);

        // [ĐÃ SỬA] 1. Lấy mã ID của người dùng đang đăng nhập (userId)
        String currentUserId = "";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // [ĐÃ SỬA] 2. Truyền currentUserId vào bên trong getAllTasks(...)
        AppDatabase.getDatabase(requireContext()).scheduleDao().getAllTasks(currentUserId).observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.updateTasks(tasks);
            }
        });

        View btnAdd = view.findViewById(R.id.btnAddTask);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateScheduleActivity.class);
            startActivity(intent);
        });

        return view;
    }
}