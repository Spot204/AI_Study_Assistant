package com.example.aistudyassistant.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.aistudyassistant.data.repository.DocumentRepository;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySessionRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.database.AppDatabase;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        
        // 1. Đồng bộ User Profile
        new UserRepository(db.userDao()).syncUnsyncedUsers();
        
        // 2. Đồng bộ Study Sessions (Phiên học & Chat)
        new StudySessionRepository(db.studySessionDao()).syncUnsyncedSessions();
        
        // 3. Đồng bộ Study Sets (Bộ học tập)
        new StudySetRepository(db.studySetDao()).syncUnsyncedStudySets();
        
        // 4. Đồng bộ Flashcards (Thẻ ghi nhớ)
        new FlashcardRepository(db.flashcardDao()).syncUnsyncedFlashcards();

        // 5. Đồng bộ Documents (Tài liệu quét OCR)
        new DocumentRepository(db.documentDao()).uploadUnsyncedDocumentsToServer();

        return Result.success();
    }
}
