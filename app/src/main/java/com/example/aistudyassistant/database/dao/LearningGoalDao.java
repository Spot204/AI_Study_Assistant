package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.LearningGoalEntity;
import java.util.List;

@Dao
public interface LearningGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGoal(LearningGoalEntity goal);

    @Update
    void updateGoal(LearningGoalEntity goal);

    @Delete
    void deleteGoal(LearningGoalEntity goal);

    @Query("SELECT * FROM learning_goals WHERE userId = :userId")
    List<LearningGoalEntity> getGoalsByUser(String userId);

    @Query("SELECT * FROM learning_goals WHERE goalId = :goalId LIMIT 1")
    LearningGoalEntity getGoalById(String goalId);

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM learning_goals WHERE syncStatus != 'synced'")
    List<LearningGoalEntity> getUnsyncedGoals();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM learning_goals")
    long getMaxUpdatedAt();
}