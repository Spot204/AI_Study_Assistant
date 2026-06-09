package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.data.model.FlashcardFirestore;
import com.example.aistudyassistant.database.dao.FlashcardDao;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
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

    private String getUserCollectionPath() {
        if (auth.getCurrentUser() != null) {
            return "users/" + auth.getCurrentUser().getUid() + "/flashcards";
        }
        return null;
    }

    public void insertFlashcards(List<FlashcardEntity> flashcards) {
        if (flashcards == null || flashcards.isEmpty()) return;

        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            for (FlashcardEntity card : flashcards) {
                card.setUpdatedAt(System.currentTimeMillis());
                card.setSyncStatus("pending_insert");
                flashcardDao.insertFlashcard(card);
            }
            syncUnsyncedFlashcards();
        });
    }

    public void updateFlashcard(FlashcardEntity flashcard) {
        flashcard.setUpdatedAt(System.currentTimeMillis());
        flashcard.setSyncStatus("pending_update");
        executorService.execute(() -> {
            flashcardDao.updateFlashcard(flashcard);
            syncToCloud(flashcard);
        });
    }

    private void syncToCloud(FlashcardEntity flashcard) {
        String path = getUserCollectionPath();
        if (path == null) return;

        FlashcardFirestore cloudModel = new FlashcardFirestore(flashcard);
        firestore.collection(path)
                .document(flashcard.getFlashcardId())
                .set(cloudModel)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    flashcard.setSyncStatus("synced");
                    flashcardDao.updateFlashcard(flashcard);
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Flashcard sync failed", e));
    }

    public void syncUnsyncedFlashcards() {
        executorService.execute(() -> {
            List<FlashcardEntity> unsyncedList = flashcardDao.getUnsyncedFlashcards();
            if (unsyncedList == null || unsyncedList.isEmpty()) return;

            String path = getUserCollectionPath();
            if (path == null) return;

            WriteBatch batch = firestore.batch();
            List<FlashcardEntity> cardsToUpdateLocal = new ArrayList<>();

            for (FlashcardEntity card : unsyncedList) {
                FlashcardFirestore dto = new FlashcardFirestore(card);
                batch.set(firestore.collection(path).document(card.getFlashcardId()), dto);
                card.setSyncStatus("synced");
                cardsToUpdateLocal.add(card);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                for (FlashcardEntity card : cardsToUpdateLocal) {
                    flashcardDao.updateFlashcard(card);
                }
            }));
        });
    }

    public void downloadNewFlashcardsFromServer() {
        executorService.execute(() -> {
            String path = getUserCollectionPath();
            if (path == null) return;

            long maxUpdatedAt = flashcardDao.getMaxUpdatedAt();

            firestore.collection(path)
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<FlashcardFirestore> remoteCards = queryDocumentSnapshots.toObjects(FlashcardFirestore.class);
                            for (FlashcardFirestore remoteCard : remoteCards) {
                                flashcardDao.insertFlashcard(remoteCard.toEntity());
                            }
                        }
                    }));
        });
    }
}
