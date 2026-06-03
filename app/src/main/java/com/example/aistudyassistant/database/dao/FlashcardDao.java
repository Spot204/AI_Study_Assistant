package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFlashcard(FlashcardEntity flashcard);

    @Update
    void updateFlashcard(FlashcardEntity flashcard);

    @Delete
    void deleteFlashcard(FlashcardEntity flashcard);

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    List<FlashcardEntity> getFlashcardsBySet(String setId);

    @Query("SELECT * FROM flashcards WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC")
    List<FlashcardEntity> getCardsToReview(long currentTime);

    @Query("SELECT COUNT(*) FROM flashcards WHERE setId = :setId")
    int getCountBySet(String setId);
}
