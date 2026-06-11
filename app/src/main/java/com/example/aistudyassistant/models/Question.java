package com.example.aistudyassistant.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model đại diện cho một câu hỏi Quiz, tương thích 100% với JSON từ AI và Database
 */
public class Question {
    // Hỗ trợ cả 3 cách đặt tên mà AI thường dùng để tránh mất dữ liệu
    @SerializedName(value = "question", alternate = {"questionText", "text"})
    private String question;

    @SerializedName("options")
    private List<String> options;

    @SerializedName("correct")
    private int correct; // 1-based index (1, 2, 3, 4)

    public Question(String question, List<String> options, int correct) {
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    public String getQuestionText() {
        if (question == null || question.trim().isEmpty()) {
            return "Nội dung câu hỏi không khả dụng";
        }
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    /**
     * Trả về index dạng 0-3 để phù hợp với logic của Activity (List access)
     */
    public int getCorrectAnswerIndex() {
        // AI trả về 1-4, ta trừ đi 1 để lấy index 0-3. 
        // Nếu dữ liệu lỗi (<1), trả về 0 làm mặc định.
        return Math.max(0, correct - 1);
    }

    public int getRawCorrectValue() {
        return correct;
    }
}
