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
    androidx.lifecycle.LiveData<List<UserAchievementEntity>> getUserAchievementsLiveData(String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM user_achievements WHERE id = :id)")
    boolean isUnlocked(String id);

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY (FIREBASE)
    // =================================================================

    @Query("SELECT * FROM user_achievements WHERE syncStatus != 'synced'")
    List<UserAchievementEntity> getUnsyncedUserAchievements();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM user_achievements")
    long getMaxUpdatedAt();
}