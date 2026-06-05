package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.aistudyassistant.database.entities.AchievementEntity;
import java.util.List;

@Dao
public interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAchievements(List<AchievementEntity> achievements);

    @Query("SELECT * FROM achievements")
    List<AchievementEntity> getAllAchievements();

    @Query("SELECT * FROM achievements WHERE achievementId = :id LIMIT 1")
    AchievementEntity getAchievementById(String id);
}