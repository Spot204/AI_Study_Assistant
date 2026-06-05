package com.example.aistudyassistant.data.repository;

import androidx.lifecycle.LiveData;

import com.example.aistudyassistant.database.dao.UserDao;
import com.example.aistudyassistant.database.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private static final String COLLECTION_NAME = "users";

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * TẠO MỚI: Lưu thông tin User mới (Dùng khi Đăng ký tài khoản thành công)
     */
    public void saveUser(User user) {
        executorService.execute(() -> {
            // Đánh dấu trạng thái chờ đồng bộ và cập nhật thời gian
            user.setSyncStatus("pending_insert");
            user.setUpdatedAt(System.currentTimeMillis());

            // 1. Lưu ngay xuống SQLite (Local)
            userDao.insertUser(user);

            // 2. Đẩy lên Firebase Firestore (Cloud)
            firestore.collection(COLLECTION_NAME)
                    .document(user.getUserId())
                    .set(user, SetOptions.merge()) // Dùng merge để không ghi đè mất dữ liệu cũ nếu có
                    .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                        // Nếu đẩy lên Cloud thành công -> Đổi trạng thái Local thành 'synced'
                        user.setSyncStatus("synced");
                        userDao.updateUser(user);
                    }));
        });
    }

    /**
     * LẤY DỮ LIỆU: Trả về LiveData để giao diện tự động cập nhật khi có thay đổi
     * (Dùng cho màn hình ProfileFragment)
     */
    public LiveData<User> getUserById(String userId) {
        return userDao.getUserById(userId);
    }

    /**
     * CẬP NHẬT: Lưu các thay đổi thông tin cá nhân (Ví dụ: Đổi tên, cập nhật avatar)
     */
    public void updateUser(User user) {
        user.setUpdatedAt(System.currentTimeMillis());
        user.setSyncStatus("pending_update");

        executorService.execute(() -> {
            // Cập nhật Local
            userDao.updateUser(user);

            // Cập nhật Cloud
            firestore.collection(COLLECTION_NAME)
                    .document(user.getUserId())
                    .set(user, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                        user.setSyncStatus("synced");
                        userDao.updateUser(user);
                    }));
        });
    }

    // =================================================================
    // 💥 LUỒNG ĐỒNG BỘ NGẦM (DÙNG KHI MẤT MẠNG RỒI CÓ MẠNG LẠI)
    // =================================================================

    /**
     * Quét các trạng thái người dùng bị treo do rớt mạng và đẩy lại lên Cloud
     */
    public void syncUnsyncedUsers() {
        executorService.execute(() -> {
            List<User> unsyncedUsers = userDao.getUnsyncedUsers();
            if (unsyncedUsers == null || unsyncedUsers.isEmpty()) return;

            for (User user : unsyncedUsers) {
                firestore.collection(COLLECTION_NAME)
                        .document(user.getUserId())
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                            user.setSyncStatus("synced");
                            userDao.updateUser(user);
                        }));
            }
        });
    }
}