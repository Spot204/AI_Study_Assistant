package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.FlashcardDao;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardRepository {
    private static final String TAG = "FlashcardRepository";
    private final FlashcardDao flashcardDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public FlashcardRepository(FlashcardDao flashcardDao) {
        this.flashcardDao = flashcardDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertFlashcards(List<FlashcardEntity> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) return;

        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            for (FlashcardEntity card : flashcards) {
                card.setUpdatedAt(now);
                card.setSyncStatus("pending_insert");
                flashcardDao.insertFlashcard(card);
            }
            syncToCloud(flashcards);
        });
    }

    private void syncToCloud(List<FlashcardEntity> flashcards) {
        String uid = auth.getUid();
        if (uid == null) return;

        WriteBatch batch = firestore.batch();
        String path = "users/" + uid + "/flashcards";

        for (FlashcardEntity card : flashcards) {
            batch.set(firestore.collection(path).document(card.getFlashcardId()), card);
        }

        batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
            for (FlashcardEntity card : flashcards) {
                card.setSyncStatus("synced");
                flashcardDao.updateFlashcard(card);
            }
            Log.d(TAG, "Flashcards synced to cloud");
        })).addOnFailureListener(e -> Log.e(TAG, "Flashcard sync failed", e));
    }

    public void syncUnsyncedFlashcards() {
        executorService.execute(() -> {
            List<FlashcardEntity> unsynced = flashcardDao.getUnsyncedFlashcards();
            if (unsynced != null && !unsynced.isEmpty()) {
                syncToCloud(unsynced);
            }
        });
    }
}
