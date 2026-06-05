package com.example.aistudyassistant.data.repository;

import com.example.aistudyassistant.database.dao.DocumentDao;
import com.example.aistudyassistant.database.entities.DocumentEntity;
import com.example.aistudyassistant.data.model.DocumentFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentRepository {
    private final DocumentDao documentDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private static final String COLLECTION_NAME = "documents";

    public DocumentRepository(DocumentDao documentDao) {
        this.documentDao = documentDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Thêm một tài liệu mới sau khi Service đã quét OCR và tóm tắt xong (Offline-First)
     */
    public void insertDocument(DocumentEntity document) {
        executorService.execute(() -> {
            // Bước 1: Lưu tức thì vào SQLite dưới máy để UI hiển thị luôn, không bắt user chờ mạng
            documentDao.insertDocument(document);

            // Bước 2: Đóng gói sang khuôn Firestore DTO và thử đẩy lên Cloud đám mây
            DocumentFirestore cloudModel = new DocumentFirestore(document);
            firestore.collection(COLLECTION_NAME)
                    .document(document.getDocumentId())
                    .set(cloudModel)
                    .addOnSuccessListener(aVoid -> {
                        // Nếu đẩy lên mạng thành công -> Chuyển trạng thái local thành 'synced'
                        executorService.execute(() -> {
                            document.setSyncStatus("synced");
                            documentDao.updateDocument(document);
                        });
                    });
            // Nếu mất mạng, file vẫn nằm an toàn ở local với trạng thái mặc định 'pending_insert'
        });
    }

    /**
     * Cập nhật thông tin tài liệu (Ví dụ: Người dùng sửa tiêu đề tài liệu)
     */
    public void updateDocument(DocumentEntity document) {
        document.setUpdatedAt(System.currentTimeMillis());
        document.setSyncStatus("pending_update");

        executorService.execute(() -> {
            documentDao.updateDocument(document);

            DocumentFirestore cloudModel = new DocumentFirestore(document);
            firestore.collection(COLLECTION_NAME)
                    .document(document.getDocumentId())
                    .set(cloudModel)
                    .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                        document.setSyncStatus("synced");
                        documentDao.updateDocument(document);
                    }));
        });
    }

    /**
     * Xóa tài liệu ở cả dưới máy lẫn trên Cloud
     */
    public void deleteDocument(DocumentEntity document) {
        executorService.execute(() -> {
            documentDao.deleteDocument(document);
            firestore.collection(COLLECTION_NAME)
                    .document(document.getDocumentId())
                    .delete();
        });
    }

    // =================================================================
    // 💥 LUỒNG ĐỒNG BỘ NGẦM (SAU KHI CÓ MẠNG TRỞ LẠI)
    // =================================================================

    /**
     * HÀM UPLOAD: Quét các tài liệu tạo offline dưới máy và đẩy loạt lên Firebase
     */
    public void uploadUnsyncedDocumentsToServer() {
        executorService.execute(() -> {
            List<DocumentEntity> unsyncedList = documentDao.getUnsyncedDocuments();
            if (unsyncedList == null || unsyncedList.isEmpty()) return;

            WriteBatch batch = firestore.batch();
            List<DocumentEntity> docsToUpdateLocal = new ArrayList<>();

            for (DocumentEntity doc : unsyncedList) {
                DocumentFirestore dto = new DocumentFirestore(doc);
                batch.set(firestore.collection(COLLECTION_NAME).document(doc.getDocumentId()), dto);

                doc.setSyncStatus("synced");
                docsToUpdateLocal.add(doc);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                for (DocumentEntity doc : docsToUpdateLocal) {
                    documentDao.updateDocument(doc);
                }
            }));
        });
    }

    /**
     * HÀM DOWNLOAD: Kéo các tài liệu mới mà user quét từ thiết bị khác về máy này
     */
    public void downloadNewDocumentsFromServer(String userId) {
        executorService.execute(() -> {
            long maxUpdatedAt = documentDao.getMaxUpdatedAt();

            firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentFirestore> remoteDocs = queryDocumentSnapshots.toObjects(DocumentFirestore.class);
                            for (DocumentFirestore remoteDoc : remoteDocs) {
                                documentDao.insertDocument(remoteDoc.toEntity());
                            }
                        }
                    }));
        });
    }
}