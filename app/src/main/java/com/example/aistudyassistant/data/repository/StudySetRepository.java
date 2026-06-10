package com.example.aistudyassistant.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.aistudyassistant.data.model.StudySetFirestore;
import com.example.aistudyassistant.database.dao.StudySetDao;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudySetRepository {
    private static final String TAG = "StudySetRepository";
    private final StudySetDao studySetDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;

    public StudySetRepository(StudySetDao studySetDao) {
        this.studySetDao = studySetDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    private String getUserCollectionPath() {
        if (auth.getCurrentUser() != null) {
            return "users/" + auth.getCurrentUser().getUid() + "/study_sets";
        }
        return null;
    }

    public void insertSet(StudySetEntity studySet, OnSuccessCallback callback) {
        if (auth.getCurrentUser() != null) {
            studySet.setUserId(auth.getCurrentUser().getUid());
        }
        studySet.setUpdatedAt(System.currentTimeMillis());
        studySet.setSyncStatus("pending_insert");
        executorService.execute(() -> {
            studySetDao.insertSet(studySet);
            syncToCloud(studySet);
            if (callback != null) {
                callback.onSuccess();
            }
        });
    }

    public void updateSet(StudySetEntity studySet) {
        studySet.setUpdatedAt(System.currentTimeMillis());
        studySet.setSyncStatus("pending_update");
        executorService.execute(() -> {
            studySetDao.updateSet(studySet);
            syncToCloud(studySet);
        });
    }

    public LiveData<List<StudySetEntity>> getAllSetsByUser(String userId) {
        return studySetDao.getAllSetsByUser(userId);
    }

    private void syncToCloud(StudySetEntity studySet) {
        String path = getUserCollectionPath();
        if (path == null) return;

        StudySetFirestore cloudModel = new StudySetFirestore(studySet);
        firestore.collection(path)
                .document(studySet.getSetId())
                .set(cloudModel)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    studySet.setSyncStatus("synced");
                    studySetDao.updateSet(studySet);
                    Log.d(TAG, "StudySet synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "StudySet sync failed", e));
    }

    public void syncUnsyncedStudySets() {
        executorService.execute(() -> {
            List<StudySetEntity> unsyncedList = studySetDao.getUnsyncedStudySets();
            if (unsyncedList == null || unsyncedList.isEmpty()) return;

            String path = getUserCollectionPath();
            if (path == null) return;

            WriteBatch batch = firestore.batch();
            List<StudySetEntity> setsToUpdateLocal = new ArrayList<>();

            for (StudySetEntity set : unsyncedList) {
                StudySetFirestore dto = new StudySetFirestore(set);
                batch.set(firestore.collection(path).document(set.getSetId()), dto);
                set.setSyncStatus("synced");
                setsToUpdateLocal.add(set);
            }

            batch.commit().addOnSuccessListener(aVoid -> executorService.execute(() -> {
                for (StudySetEntity set : setsToUpdateLocal) {
                    studySetDao.updateSet(set);
                }
            }));
        });
    }

    public void downloadNewStudySetsFromServer() {
        executorService.execute(() -> {
            String path = getUserCollectionPath();
            if (path == null) return;

            long maxUpdatedAt = studySetDao.getMaxUpdatedAt();

            firestore.collection(path)
                    .whereGreaterThan("updatedAt", maxUpdatedAt)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> executorService.execute(() -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            List<StudySetFirestore> remoteSets = queryDocumentSnapshots.toObjects(StudySetFirestore.class);
                            for (StudySetFirestore remoteSet : remoteSets) {
                                studySetDao.insertSet(remoteSet.toEntity());
                            }
                        }
                    }));
        });
    }

    public interface OnSuccessCallback {
        void onSuccess();
    }
}
