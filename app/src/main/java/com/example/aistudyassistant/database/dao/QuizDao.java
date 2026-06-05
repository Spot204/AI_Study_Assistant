package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.QuizEntity;
import java.util.List;

@Dao
public interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuiz(QuizEntity quiz);

    @Update
    void updateQuiz(QuizEntity quiz);

    @Delete
    void deleteQuiz(QuizEntity quiz);

    @Query("SELECT * FROM quizzes WHERE userId = :userId")
    List<QuizEntity> getQuizzesByUser(String userId);

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId LIMIT 1")
    QuizEntity getQuizById(String quizId);

    // =================================================================
    // 💥 CÁC HÀM PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM quizzes WHERE syncStatus != 'synced'")
    List<QuizEntity> getUnsyncedQuizzes();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM quizzes")
    long getMaxUpdatedAt();
}