package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class FlashcardEntity {

    @PrimaryKey
    @NonNull
    private String flashcardId; // Mã ID duy nhất của thẻ (UUID)
    private String setId; // Mã ID của bộ học phần lớn chứa thẻ này (Liên kết với STUDY_SETS)
    private String userId; // Mã ID người dùng tạo thẻ
    private String front; // Nội dung câu hỏi/Từ vựng mặt trước thẻ
    private String back; // Nội dung câu trả lời/Định nghĩa mặt sau thẻ
    private String aiExplanation; // Đoạn giải thích chi tiết bổ sung do AI sinh ra (Có thể null)
    private int box; // Số thứ tự hộp Leitner (Mặc định bằng 1 khi tạo mới)
    private long nextReviewAt; // Mốc thời gian (Timestamp) thuật toán hẹn giờ ôn tập lại thẻ này

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian chỉnh sửa nội dung thẻ cuối cùng
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert", "pending_update"

    /**
     * Constructor sử dụng khi người dùng tự tạo thẻ bằng tay hoặc AI sinh thẻ hàng loạt
     */
    public FlashcardEntity(@NonNull String flashcardId, String setId, String userId, String front, String back) {
        this.flashcardId = flashcardId;
        this.setId = setId;
        this.userId = userId;
        this.front = front;
        this.back = back;
        this.box = 1; // Mặc định nằm ở hộp số 1 khi mới tạo
        this.nextReviewAt = System.currentTimeMillis(); // Mặc định cho phép học/ôn tập ngay lập tức

        // Tự động cấu hình các mốc thời gian đồng bộ mặc định khi tạo mới ở local
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getFlashcardId() { return flashcardId; }
    public void setFlashcardId(@NonNull String flashcardId) { this.flashcardId = flashcardId; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public int getBox() { return box; }
    public void setBox(int box) { this.box = box; }

    public long getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(long nextReviewAt) { this.nextReviewAt = nextReviewAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}