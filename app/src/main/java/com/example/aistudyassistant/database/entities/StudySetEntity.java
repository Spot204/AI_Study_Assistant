package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_sets")
public class StudySetEntity {

    @PrimaryKey
    @NonNull
    private String setId; // Mã ID duy nhất của bộ học phần (UUID String)
    private String userId; // Mã ID của user sở hữu bộ này
    private String title; // Tên bộ học phần (Ví dụ: "Từ vựng Tiếng Anh Chuyên Ngành")
    private String visibility; // Chế độ hiển thị: "public" | "private"
    private int totalCards; // Tổng số lượng thẻ flashcard đang nằm trong bộ này
    private float masteryPercentage; // Tỷ lệ phần trăm người dùng đã thuộc bộ thẻ này

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian chỉnh sửa cuối cùng (Timestamp)
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert", "pending_update"

    /**
     * Constructor thực tế của bạn, đã được tối ưu hóa các giá trị mặc định ban đầu
     */
    public StudySetEntity(@NonNull String setId, String userId, String title) {
        this.setId = setId;
        this.userId = userId;
        this.title = title;
        this.visibility = "private"; // Giữ nguyên thiết lập mặc định bảo mật của bạn

        this.totalCards = 0; // Mặc định bộ mới tạo chưa có thẻ nào
        this.masteryPercentage = 0.0f; // Mặc định tỷ lệ thuộc bài bằng 0

        // Tự động gán thời gian hiện tại của thiết bị khi tạo mới
        this.updatedAt = System.currentTimeMillis();
        // Mặc định ban đầu khi tạo ở máy local là đang chờ đẩy lên cloud
        this.syncStatus = "pending_insert";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getSetId() { return setId; }
    public void setSetId(@NonNull String setId) { this.setId = setId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public int getTotalCards() { return totalCards; }
    public void setTotalCards(int totalCards) { this.totalCards = totalCards; }

    public float getMasteryPercentage() { return masteryPercentage; }
    public void setMasteryPercentage(float masteryPercentage) { this.masteryPercentage = masteryPercentage; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}