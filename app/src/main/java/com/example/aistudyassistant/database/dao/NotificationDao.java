package com.example.aistudyassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.NotificationEntity;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Update
    void updateNotification(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<NotificationEntity> getNotificationsByUser(String userId);

    @Query("UPDATE notifications SET isRead = 1, updatedAt = :updateTime, syncStatus = 'pending_update' WHERE notificationId = :id")
    void markAsRead(String id, long updateTime); // Cập nhật cả thời gian sửa đổi và trạng thái chờ đồng bộ

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadCount(String userId);

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY
    // =================================================================

    @Query("SELECT * FROM notifications WHERE syncStatus != 'synced'")
    List<NotificationEntity> getUnsyncedNotifications();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM notifications")
    long getMaxUpdatedAt();
}