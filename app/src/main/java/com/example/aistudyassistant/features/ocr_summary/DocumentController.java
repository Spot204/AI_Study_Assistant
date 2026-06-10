package com.example.aistudyassistant.features.ocr_summary;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.DocumentRepository;
import com.example.aistudyassistant.data.repository.FlashcardRepository;
import com.example.aistudyassistant.data.repository.StudySetRepository;
import com.example.aistudyassistant.data.repository.UserStatsRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.DocumentEntity;
import com.example.aistudyassistant.database.entities.FlashcardEntity;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.example.aistudyassistant.services.core.LLMService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentController {
    private final DocumentRepository documentRepository;
    private final StudySetRepository studySetRepository;
    private final FlashcardRepository flashcardRepository;
    private final LLMService llmService;

    public interface AIProcessCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public interface FlashcardGenerationCallback {
        void onSuccess(int count);
        void onError(String error);
    }

    public DocumentController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.documentRepository = new DocumentRepository(db.documentDao());
        this.studySetRepository = new StudySetRepository(db.studySetDao());
        this.flashcardRepository = new FlashcardRepository(db.flashcardDao(), 
                new UserStatsRepository(db.userStatsDao(), db.achievementDao(), db.userAchievementDao()));
        this.llmService = new LLMService();
    }

    public void summarizeDocument(String text, AIProcessCallback callback) {
        llmService.generateResponseAsync("Hãy tóm tắt ngắn gọn và phân tích nội dung này: " + text, response -> {
            if (response != null && !response.isEmpty()) {
                callback.onSuccess(response);
            } else {
                callback.onError("Không nhận được phản hồi từ AI");
            }
        });
    }

    public void generateFlashcardsFromSummary(String summary, String userId, FlashcardGenerationCallback callback) {
        llmService.generateResponseAsync("Từ nội dung sau, hãy tạo 3 flashcards. Mỗi flashcard gồm Front (câu hỏi) và Back (câu trả lời). Định dạng: 'Front: [câu hỏi] | Back: [câu trả lời]'. Nội dung: " + summary, response -> {
            try {
                String setId = UUID.randomUUID().toString();
                StudySetEntity studySet = new StudySetEntity(setId, userId, "AI Generated Set");
                
                List<FlashcardEntity> flashcards = new ArrayList<>();
                String[] lines = response.split("\n");
                for (String line : lines) {
                    if (line.contains("Front:") && line.contains("| Back:")) {
                        String front = line.substring(line.indexOf("Front:") + 6, line.indexOf("| Back:")).trim();
                        String back = line.substring(line.indexOf("| Back:") + 7).trim();
                        flashcards.add(new FlashcardEntity(UUID.randomUUID().toString(), setId, userId, front, back));
                    }
                }

                if (!flashcards.isEmpty()) {
                    studySet.setTotalCards(flashcards.size());
                    studySetRepository.insertSet(studySet, () -> {
                        flashcardRepository.insertFlashcards(flashcards);
                        callback.onSuccess(flashcards.size());
                    });
                } else {
                    callback.onError("Không thể trích xuất Flashcards từ AI");
                }
            } catch (Exception e) {
                Log.e("DocumentController", "Error saving flashcards", e);
                callback.onError("Lỗi khi lưu Flashcards: " + e.getMessage());
            }
        });
    }

    public void saveDocument(DocumentEntity doc) {
        documentRepository.insertDocument(doc);
    }

    public LiveData<List<DocumentEntity>> getAllDocuments(String userId) {
        return documentRepository.getAllDocuments(userId);
    }

    public void syncDocuments(String userId) {
        documentRepository.uploadUnsyncedDocumentsToServer();
        documentRepository.downloadNewDocumentsFromServer(userId);
    }
}
