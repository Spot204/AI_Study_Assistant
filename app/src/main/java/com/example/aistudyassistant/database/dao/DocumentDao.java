package com.example.aistudyassistant.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.DocumentEntity;
import java.util.List;

@Dao
public interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDocument(DocumentEntity document);

    @Update
    void updateDocument(DocumentEntity document);

    @Delete
    void deleteDocument(DocumentEntity document);

    @Query("SELECT * FROM documents WHERE userId = :userId ORDER BY uploadedAt DESC")
    LiveData<List<DocumentEntity>> getDocumentsByUser(String userId);

    @Query("SELECT * FROM documents WHERE documentId = :id LIMIT 1")
    DocumentEntity getDocumentById(String id);

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM documents WHERE syncStatus != 'synced'")
    List<DocumentEntity> getUnsyncedDocuments();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM documents")
    long getMaxUpdatedAt();
}