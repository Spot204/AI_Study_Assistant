package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
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

    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    List<StudySessionEntity> getSessionsByUser(String userId);

    @Query("SELECT * FROM study_sessions WHERE sessionId = :sessionId LIMIT 1")
    StudySessionEntity getSessionById(String sessionId);

    @Query("SELECT * FROM study_sessions WHERE isSynced = 0")
    List<StudySessionEntity> getUnsyncedSessions();
}
