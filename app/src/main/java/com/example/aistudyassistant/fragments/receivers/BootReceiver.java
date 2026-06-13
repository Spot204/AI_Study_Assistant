package com.example.aistudyassistant.fragments.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.aistudyassistant.data.repository.ScheduleRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId != null) {
                ScheduleRepository repository = new ScheduleRepository(context, 
                        AppDatabase.getDatabase(context).scheduleDao());
                
                new Thread(() -> {
                    // Lấy tất cả task chưa hoàn thành của user hiện tại
                    List<ScheduleTask> tasks = AppDatabase.getDatabase(context)
                            .scheduleDao().getTasksByDate(userId, ""); // Cần một hàm lấy all tasks
                    // Tạm thời sử dụng một luồng lặp để set lại alarm nếu cần
                }).start();
            }
        }
    }
}
