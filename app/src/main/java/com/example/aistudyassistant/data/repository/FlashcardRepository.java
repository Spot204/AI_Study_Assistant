package com.example.aistudyassistant.data.repository;

import com.example.aistudyassistant.database.dao.FlashcardDao;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardRepository {
    private final FlashcardDao flashcardDao;
    private final ExecutorService executorService;

    public FlashcardRepository(FlashcardDao flashcardDao) {
        this.flashcardDao = flashcardDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertFlashcards(List<FlashcardEntity> flashcards) {
        executorService.execute(() -> {
            for (FlashcardEntity card : flashcards) {
                flashcardDao.insertFlashcard(card);
            }
        });
    }
}
