package com.example.aistudyassistant.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class StudySessionWithTitle {
    @Embedded
    public StudySessionEntity session;

    public String studySetTitle; // Sẽ được gán thủ công hoặc qua Query
}
