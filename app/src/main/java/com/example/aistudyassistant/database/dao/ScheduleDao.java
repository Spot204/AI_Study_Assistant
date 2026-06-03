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

    @Query("SELECT * FROM schedule_tasks ORDER BY startTime ASC")
    androidx.lifecycle.LiveData<List<ScheduleTask>> getAllTasks();

    @Query("SELECT * FROM schedule_tasks WHERE date = :date ORDER BY startTime ASC")
    List<ScheduleTask> getTasksByDate(String date);
}
