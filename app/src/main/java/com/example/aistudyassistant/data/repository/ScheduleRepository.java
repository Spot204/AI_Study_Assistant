package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.ScheduleDao;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private final ScheduleDao scheduleDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public ScheduleRepository(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertTask(ScheduleTask task) {
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(UUID.randomUUID().toString());
        }
        task.setUpdatedAt(System.currentTimeMillis());
        task.setSyncStatus("pending_insert");

        executorService.execute(() -> {
            scheduleDao.insertTask(task);
            syncToCloud(task);
        });
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
        executorService.execute(() -> {
            String uid = auth.getUid();
            if (uid == null) return;

            long maxUpdatedAt = scheduleDao.getMaxUpdatedAt();

            firestore.collection("users").document(uid)
                    .collection("schedules")
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<ScheduleTask> remoteTasks = queryDocumentSnapshots.toObjects(ScheduleTask.class);
                            for (ScheduleTask remoteTask : remoteTasks) {
                                remoteTask.setSyncStatus("synced");
                                scheduleDao.insertTask(remoteTask);
                            }
                        }
                    }));
        });
    }
}
