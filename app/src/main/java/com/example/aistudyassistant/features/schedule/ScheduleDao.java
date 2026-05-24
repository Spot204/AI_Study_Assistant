package com.example.aistudyassistant.features.schedule;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ScheduleDao {
    @Insert
    void insert(ScheduleTask task);

    @Update
    void update(ScheduleTask task);

    @Delete
    void delete(ScheduleTask task);

    @Query("SELECT * FROM schedule_tasks ORDER BY startTime ASC")
    LiveData<List<ScheduleTask>> getAllTasks();

    @Query("SELECT * FROM schedule_tasks WHERE date = :date ORDER BY startTime ASC")
    LiveData<List<ScheduleTask>> getTasksByDate(String date);
}
