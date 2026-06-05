package com.example.aistudyassistant.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aistudyassistant.database.entities.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    LiveData<User> getUserById(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<User> getUserByEmail(String email);

    @Query("SELECT * FROM users LIMIT 1")
    LiveData<User> getAnyUser();

    @Query("DELETE FROM users")
    void deleteAll();

    // =================================================================
    // 💥 CÁC HÀM BỔ SUNG PHỤC VỤ LUỒNG ĐỒNG BỘ ĐÁM MÂY (FIREBASE)
    // =================================================================

    @Query("SELECT * FROM users WHERE syncStatus != 'synced'")
    List<User> getUnsyncedUsers();

    @Query("SELECT COALESCE(MAX(updatedAt), 0) FROM users")
    long getMaxUpdatedAt();
}