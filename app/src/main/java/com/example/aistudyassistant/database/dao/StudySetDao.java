package com.example.aistudyassistant.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.List;

@Dao
public interface StudySetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSet(StudySetEntity studySet);

    @Update
    void updateSet(StudySetEntity studySet);

    @Delete
    void deleteSet(StudySetEntity studySet);

    @Query("SELECT * FROM study_sets WHERE userId = :userId")
    LiveData<List<StudySetEntity>> getAllSetsByUser(String userId);

    @Query("SELECT * FROM study_sets WHERE setId = :setId LIMIT 1")
    StudySetEntity getSetById(String setId);

    // =================================================================
    // 💥 CÁC HÀM PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM study_sets WHERE syncStatus != 'synced'")
    List<StudySetEntity> getUnsyncedStudySets();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM study_sets")
    long getMaxUpdatedAt();
}