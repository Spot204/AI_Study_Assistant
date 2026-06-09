package com.example.aistudyassistant.features.ocr_summary;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.repository.DocumentRepository;
import com.example.aistudyassistant.database.AppDatabase;
import com.example.aistudyassistant.database.entities.DocumentEntity;
import java.util.List;

public class DocumentController {
    private final DocumentRepository documentRepository;

    public DocumentController(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.documentRepository = new DocumentRepository(db.documentDao());
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
