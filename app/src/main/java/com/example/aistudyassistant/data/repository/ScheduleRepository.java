package com.example.aistudyassistant.data.repository;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.aistudyassistant.database.dao.ScheduleDao;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import com.example.aistudyassistant.fragments.receivers.AlarmReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private final ScheduleDao scheduleDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final Context context;

    public ScheduleRepository(Context context, ScheduleDao scheduleDao) {
        this.context = context.getApplicationContext();
        this.scheduleDao = scheduleDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertTask(ScheduleTask task) {
        String currentUid = auth.getUid();
        if (task.getUserId() == null || task.getUserId().isEmpty()) {
            if (currentUid != null) task.setUserId(currentUid);
        }
        
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(UUID.randomUUID().toString());
        }
        task.setUpdatedAt(System.currentTimeMillis());
        task.setSyncStatus("pending_insert");

        executorService.execute(() -> {
            scheduleDao.insertTask(task);
            scheduleAlarm(task);
            syncToCloud(task);
        });
    }

    public void scheduleAlarm(ScheduleTask task) {
        if (task.getReminderMinutes() < 0) return;

        try {
            // Định dạng ngày: dd/MM/yyyy, Giờ: hh:mm a (Khớp với CreateScheduleFragment)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            String dateTimeStr = task.getDate() + " " + task.getStartTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateTimeStr));

            // Trừ đi số phút nhắc nhở
            calendar.add(Calendar.MINUTE, -task.getReminderMinutes());

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.w(TAG, "Thời gian nhắc nhở đã qua, không đặt báo thức.");
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("title", task.getTitle());
            intent.putExtra("taskId", task.getTaskId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getTaskId().hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d(TAG, "Đã đặt báo thức cho task: " + task.getTitle() + " lúc " + calendar.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đặt báo thức: " + e.getMessage());
        }
    }

    public LiveData<List<ScheduleTask>> getAllTasks(String userId) {
        return scheduleDao.getAllTasks(userId);
    }

    public void updateTask(ScheduleTask task) {
        task.setUpdatedAt(System.currentTimeMillis());
        task.setSyncStatus("pending_update");

        executorService.execute(() -> {
            scheduleDao.updateTask(task);
            syncToCloud(task);
        });
    }

    public void deleteTask(ScheduleTask task) {
        executorService.execute(() -> {
            scheduleDao.deleteTask(task);
            String uid = auth.getUid();
            if (uid != null) {
                firestore.collection("users").document(uid)
                        .collection("schedules").document(task.getTaskId())
                        .delete();
            }
        });
    }

    private void syncToCloud(ScheduleTask task) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("schedules").document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    task.setSyncStatus("synced");
                    scheduleDao.updateTask(task);
                    Log.d(TAG, "ScheduleTask synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "ScheduleTask sync failed", e));
    }

    public void syncUnsyncedTasks() {
        executorService.execute(() -> {
            List<ScheduleTask> unsynced = scheduleDao.getUnsyncedTasks();
            if (unsynced != null && !unsynced.isEmpty()) {
                for (ScheduleTask task : unsynced) {
                    syncToCloud(task);
                }
            }
        });
    }

    public void downloadNewTasksFromServer() {
        String uid = auth.getUid();
        if (uid == null) return;

        long maxUpdatedAt = scheduleDao.getMaxUpdatedAt();

        try {
            com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task = 
                firestore.collection("users").document(uid)
                    .collection("schedules")
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get();
            
            com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots = com.google.android.gms.tasks.Tasks.await(task);
            
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                List<ScheduleTask> remoteTasks = queryDocumentSnapshots.toObjects(ScheduleTask.class);
                for (ScheduleTask remoteTask : remoteTasks) {
                    remoteTask.setSyncStatus("synced");
                    scheduleDao.insertTask(remoteTask);
                    Log.d(TAG, "Downloaded task: " + remoteTask.getTitle());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải lịch trình: " + e.getMessage());
        }
    }
}
