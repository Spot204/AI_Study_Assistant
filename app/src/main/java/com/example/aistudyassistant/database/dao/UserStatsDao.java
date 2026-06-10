package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.UserStatsEntity;
import java.util.List;

@Dao
public interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStats(UserStatsEntity stats);

    @Update
    void updateStats(UserStatsEntity stats);

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    UserStatsEntity getStatsByUser(String userId);

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    androidx.lifecycle.LiveData<UserStatsEntity> getStatsLiveData(String userId);

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY (FIREBASE)
    // =================================================================

    @Query("SELECT * FROM user_stats WHERE syncStatus != 'synced'")
    List<UserStatsEntity> getUnsyncedUserStats();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM user_stats")
    long getMaxUpdatedAt();
}