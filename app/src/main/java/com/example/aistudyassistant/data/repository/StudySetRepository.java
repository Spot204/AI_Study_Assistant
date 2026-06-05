package com.example.aistudyassistant.data.repository;

import com.example.aistudyassistant.database.dao.StudySetDao;
import com.example.aistudyassistant.database.entities.StudySetEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudySetRepository {
    private final StudySetDao studySetDao;
    private final ExecutorService executorService;

    public StudySetRepository(StudySetDao studySetDao) {
        this.studySetDao = studySetDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insertSet(StudySetEntity studySet, OnSuccessCallback callback) {
        executorService.execute(() -> {
            studySetDao.insertSet(studySet);
            if (callback != null) {
                callback.onSuccess();
            }
        });
    }

    public interface OnSuccessCallback {
        void onSuccess();
    }
}
