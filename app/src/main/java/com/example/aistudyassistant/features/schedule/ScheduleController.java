package com.example.aistudyassistant.features.schedule;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.ScheduleRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import java.util.List;

public class ScheduleController {
    private final ScheduleRepository scheduleRepository;

    public ScheduleController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.scheduleRepository = new ScheduleRepository(db.scheduleDao());
    }

    public void addTask(ScheduleTask task) {
        scheduleRepository.insertTask(task);
    }

    public void updateTask(ScheduleTask task) {
        scheduleRepository.updateTask(task);
    }

    public void deleteTask(ScheduleTask task) {
        scheduleRepository.deleteTask(task);
    }

    public LiveData<List<ScheduleTask>> getAllTasks(String userId) {
        return AppDatabase.getDatabase(null).scheduleDao().getAllTasks(userId);
    }

    public void syncSchedule() {
        scheduleRepository.syncUnsyncedTasks();
        scheduleRepository.downloadNewTasksFromServer();
    }
}
