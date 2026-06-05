package com.example.aistudyassistant.database.entities;

public class ChatMessage {
    private String text; // Nội dung tin nhắn chat
    private boolean isUser; // True nếu là người dùng gửi, False nếu là AI trả lời
    private long timestamp; // Thời gian gửi tin nhắn (System.currentTimeMillis())

    // Constructor mặc định (Bắt buộc phải có để Gson hoặc Firebase parse dữ liệu tự động)
    public ChatMessage() {}

    public ChatMessage(String text, boolean isUser, long timestamp) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    // --- Hệ thống Getter và Setter ---
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isUser() { return isUser; }
    public void setUser(boolean user) { isUser = user; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}