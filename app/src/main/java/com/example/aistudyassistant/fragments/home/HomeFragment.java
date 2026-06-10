package com.example.aistudyassistant.fragments.home;

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
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private String userName = "Người dùng";
    private TextView tvWelcome, tvStreak, tvTip;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getDatabase(requireContext());
        tvWelcome = view.findViewById(R.id.tvWelcomeHeader);
        tvStreak = view.findViewById(R.id.tvStreakCount);
        tvTip = view.findViewById(R.id.tvDailyTipText);

        // Giá trị mặc định
        tvWelcome.setText("Chào cậu!");
        tvStreak.setText("0 ngày");
        tvTip.setText("Hãy quét tài liệu hoặc chat trực tiếp với trợ lý AI đề tự do tóm tắt các ghi chú khó nhằn nhé!");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Theo dõi thông tin User
        db.userDao().getAnyUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userName = user.getFullName();
                tvWelcome.setText("Chào cậu, " + userName + "!");
                
                // Sau khi có User, theo dõi Stats của User đó
                loadUserStats(user.getUserId());
            }
        });
    }

    private void loadUserStats(String userId) {
        // Lấy stats từ Database (LiveData sẽ tự cập nhật UI khi data thay đổi)
        new Thread(() -> {
            // Đảm bảo có bản ghi stats cho user này
            UserStatsEntity existingStats = db.userStatsDao().getStatsByUser(userId);
            if (existingStats == null) {
                db.userStatsDao().insertStats(new UserStatsEntity(userId));
            }
            
            // Quay lại UI thread để observe (vì DAO thường trả về LiveData)
            // Lưu ý: getStatsByUser trong DAO hiện tại không trả về LiveData, 
            // ta nên sửa DAO hoặc lấy trực tiếp ở đây.
            // Để đơn giản và hiệu quả, ta sẽ cập nhật UI trực tiếp nếu không dùng LiveData.
            UserStatsEntity stats = db.userStatsDao().getStatsByUser(userId);
            if (isAdded() && stats != null) {
                requireActivity().runOnUiThread(() -> {
                    tvStreak.setText(stats.getStreakCount() + " ngày liên tiếp");
                });
            }
        }).start();
    }
}
