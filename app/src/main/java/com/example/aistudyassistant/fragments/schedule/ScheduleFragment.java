package com.example.aistudyassistant.fragments.schedule;

import android.os.Bundle;
import android.util.Log;
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

// Chú ý: Đã đổi Import từ Activity sang Fragment mới
import com.example.aistudyassistant.features.schedule.ScheduleAdapter;
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

        // 1. Lấy mã ID của người dùng đang đăng nhập (userId)
        String currentUserId = "";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // 2. Tải danh sách công việc
        AppDatabase.getDatabase(requireContext()).scheduleDao().getAllTasks(currentUserId).observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.updateTasks(tasks);
            }
        });

        // 3. Xử lý nút Add Task (Mở CreateScheduleFragment)
        View btnAdd = view.findViewById(R.id.btnAddTask);
        btnAdd.setOnClickListener(v -> {
            Log.d("ScheduleFragment", "Button Add clicked - Switching to CreateScheduleFragment");

            // Lấy FragmentManager từ Activity chứa nó
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        // 1. Chỉ định hiệu ứng chuyển cảnh (Tùy chọn)
                        .setCustomAnimations(
                                android.R.anim.slide_in_left,
                                android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.slide_out_right
                        )
                        // 2. Thêm Fragment mới đè lên.
                        // R.id.fragment_container: Đây phải là ID của cái khung (FrameLayout) nằm trong MainActivity của bạn
                        .add(R.id.fragment_container, new CreateScheduleFragment())
                        // 3. Lưu vào ngăn xếp để bấm Back có thể quay lại
                        .addToBackStack("CreateSchedule")
                        .commit();
            }
        });

        return view;
    }
}