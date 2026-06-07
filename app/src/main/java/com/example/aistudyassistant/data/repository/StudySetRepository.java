package com.example.aistudyassistant.data.repository;

import android.util.Log;
import com.example.aistudyassistant.database.dao.StudySetDao;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public void insertSet(StudySetEntity studySet, OnSuccessCallback callback) {
        if (studySet.getSetId() == null || studySet.getSetId().isEmpty()) {
            studySet.setSetId(java.util.UUID.randomUUID().toString());
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

    private void syncToCloud(StudySetEntity studySet) {
        String uid = auth.getUid();
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .collection("study_sets").document(studySet.getSetId())
                .set(studySet)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    studySet.setSyncStatus("synced");
                    studySetDao.updateSet(studySet);
                    Log.d(TAG, "StudySet synced to cloud");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "StudySet sync failed", e));
    }

    public void syncUnsyncedStudySets() {
        executorService.execute(() -> {
            List<StudySetEntity> unsynced = studySetDao.getUnsyncedStudySets();
            if (unsynced != null && !unsynced.isEmpty()) {
                for (StudySetEntity set : unsynced) {
                    syncToCloud(set);
                }
            }
        });
    }

    public interface OnSuccessCallback {
        void onSuccess();
    }
}
