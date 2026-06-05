package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.ScheduleTask;
import java.util.List;

@Dao
public interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(ScheduleTask task);

    @Update
    void updateTask(ScheduleTask task);

    @Delete
    void deleteTask(ScheduleTask task);

    @Query("SELECT * FROM schedule_tasks WHERE userId = :userId ORDER BY date ASC, startTime ASC")
    androidx.lifecycle.LiveData<List<ScheduleTask>> getAllTasks(String userId);

    @Query("SELECT * FROM schedule_tasks WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    List<ScheduleTask> getTasksByDate(String userId, String date);

    // =================================================================
    // 💥 CÁC HÀM PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM schedule_tasks WHERE syncStatus != 'synced'")
    List<ScheduleTask> getUnsyncedTasks();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM schedule_tasks")
    long getMaxUpdatedAt();
}