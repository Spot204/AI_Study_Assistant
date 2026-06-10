package com.example.aistudyassistant.features.flashcard;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.List;

public class FlashcardController {
    private final FlashcardRepository flashcardRepository;
    private final StudySetRepository studySetRepository;

    public FlashcardController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.flashcardRepository = new FlashcardRepository(db.flashcardDao(), 
                new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao()));
        this.studySetRepository = new StudySetRepository(db.studySetDao());
    }

    public LiveData<List<StudySetEntity>> getStudySets(String userId) {
        return studySetRepository.getAllSetsByUser(userId);
    }

    public void createStudySet(StudySetEntity set, StudySetRepository.OnSuccessCallback callback) {
        studySetRepository.insertSet(set, callback);
    }

    public void updateStudySet(StudySetEntity set) {
        studySetRepository.updateSet(set);
    }

    public void addFlashcards(List<FlashcardEntity> cards) {
        flashcardRepository.insertFlashcards(cards);
    }

    public void updateFlashcard(FlashcardEntity card) {
        flashcardRepository.updateFlashcard(card);
    }

    public void syncFlashcards() {
        studySetRepository.syncUnsyncedStudySets();
        studySetRepository.downloadNewStudySetsFromServer();
        flashcardRepository.syncUnsyncedFlashcards();
        flashcardRepository.downloadNewFlashcardsFromServer();
    }
}
