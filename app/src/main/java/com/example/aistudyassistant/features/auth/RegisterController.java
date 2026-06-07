package com.example.aistudyassistant.features.auth;

import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;

public class RegisterController {

    private final RegisterView view;
    private final AuthService authService;
    private final UserRepository userRepository;

    public RegisterController(RegisterView view, AuthService authService, UserRepository userRepository) {
        this.view = view;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // ĐÂY LÀ TOÀN BỘ LOGIC CỦA TRANG ĐĂNG KÝ
    public void performRegister(String name, String email, String password, String confirmPassword) {
        // 1. Kiểm tra rỗng
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            view.showError("Mật khẩu xác nhận không khớp!");
            return;
        }

        view.showLoading();

        // 3. Gọi dịch vụ Firebase
        authService.Register(email, password, new AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                // Thành công: Lưu thông tin vào Room Database
                User newUser = new User(uid, name, email);
                userRepository.saveUser(newUser);

                // Báo cáo lại cho Giao diện
                view.hideLoading();
                view.showSuccess("Đăng ký thành công!");
                view.navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                // Thất bại
                view.hideLoading();
                view.showError("Đăng ký thất bại: " + error);
            }
        });
    }
}