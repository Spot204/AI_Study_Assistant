package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.aistudyassistant.database.entities.UserAchievementEntity;
import java.util.List;

@Dao
public interface UserAchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void unlockAchievement(UserAchievementEntity userAchievement);

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    List<UserAchievementEntity> getUserAchievements(String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM user_achievements WHERE id = :id)")
    boolean isUnlocked(String id);
}
