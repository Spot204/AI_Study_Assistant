package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String userId; // Mã ID duy nhất của người dùng (Nên lấy từ Firebase UID)
    private String fullName; // Họ và tên hiển thị (Ví dụ: "Nguyễn Văn A")
    private String email; // Địa chỉ email tài khoản
    private String bio; // Đoạn giới thiệu bản thân (Ví dụ: "Sinh viên năm 3 chuyên ngành IT")
    private String school; // Tên trường học của người dùng
    private String avatarPath; // Đường dẫn ảnh đại diện (Có thể là bộ nhớ máy hoặc URL Cloud)

    // 2 trường bắt buộc bổ sung để phục vụ luồng đồng bộ hóa dữ liệu cá nhân lên Firebase
    private long updatedAt; // Mốc thời gian sửa đổi thông tin cá nhân cuối cùng (Timestamp)
    private String syncStatus; // Trạng thái đồng bộ: "synced", "pending_update"

    /**
     * Constructor chính được Room DB sử dụng để tương tác dữ liệu
     */
    public User(@NonNull String userId, String fullName, String email, String bio, String school) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.bio = bio;
        this.school = school;

        // Tự động gán thời gian hiện tại của thiết bị khi tạo dữ liệu cá nhân
        this.updatedAt = System.currentTimeMillis();
        // Mặc định ban đầu ở máy local là cần cập nhật lên cloud
        this.syncStatus = "pending_update";
    }

    /**
     * Constructor phụ sử dụng khi đăng nhập nhanh (Room sẽ bỏ qua hàm này nhờ @Ignore)
     */
    @androidx.room.Ignore
    public User(@NonNull String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;

        this.updatedAt = System.currentTimeMillis();
        this.syncStatus = "pending_update";
    }

    // --- Hệ thống Getter và Setter bắt buộc cho Room DB ---

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}