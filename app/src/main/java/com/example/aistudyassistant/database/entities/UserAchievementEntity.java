package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_achievements")
public class UserAchievementEntity {

    @PrimaryKey
    @NonNull
    private String id; // Định dạng chuỗi ghép: "userId_achievementId" (UUID/String độc nhất)
    private String userId; // Mã ID người dùng đạt được danh hiệu
    private String achievementId; // Mã ID của danh hiệu hệ thống được mở khóa
    private long unlockedAt; // Mốc thời gian người dùng chính thức đạt danh hiệu này

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa dữ liệu Firebase
    private long updatedAt; // Mốc thời gian ghi nhận (Timestamp)
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_insert"

    /**
     * Constructor thực tế của bạn, tự động gán các mốc thời gian đồng bộ mặc định khi mở khóa thành công
     */
    public UserAchievementEntity(@NonNull String id, String userId, String achievementId) {
        this.id = id;
        this.userId = userId;
        this.achievementId = achievementId;
        this.unlockedAt = System.currentTimeMillis();

        // Tự động cấu hình thời gian hiện tại cho luồng ngầm Firebase quét dữ liệu
        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_insert"; // Mặc định là cần đẩy lên đám mây sao lưu
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}