package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.StudySessionEntity;
import java.util.List;

@Dao
public interface StudySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(StudySessionEntity session);

    @Update
    void updateSession(StudySessionEntity session);

    @Delete
    void deleteSession(StudySessionEntity session); // Bổ sung hàm xóa phiên học cụ thể nếu cần

    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    List<StudySessionEntity> getSessionsByUser(String userId);

    @Query("SELECT * FROM study_sessions WHERE sessionId = :sessionId LIMIT 1")
    StudySessionEntity getSessionById(String sessionId);

    @Query("DELETE FROM study_sessions")
    void deleteAll();

    // =================================================================
    // 💥 CÁC HÀM PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM study_sessions WHERE syncStatus != 'synced'")
    List<StudySessionEntity> getUnsyncedSessions();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM study_sessions")
    long getMaxUpdatedAt();
}