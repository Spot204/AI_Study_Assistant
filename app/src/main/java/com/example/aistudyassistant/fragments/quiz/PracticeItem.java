package com.example.aistudyassistant.fragments.quiz;

public class PracticeItem {
    public static final int TYPE_QUIZ = 0;
    public static final int TYPE_STUDY_SET = 1;

    private String id;
    private String title;
    private String info;
    private int type;

    public PracticeItem(String id, String title, String info, int type) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.type = type;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getInfo() { return info; }
    public int getType() { return type; }
}
