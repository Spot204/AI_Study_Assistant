package com.example.aistudyassistant.features.auth;

import com.example.aistudyassistant.database.entities.User;
import com.example.aistudyassistant.data.repository.UserRepository;
import com.example.aistudyassistant.services.auth.AuthCallback;
import com.example.aistudyassistant.services.auth.AuthService;
import com.example.aistudyassistant.features.auth.LoginView;

public class LoginController {

    private final LoginView view; // Liên kết với giao diện
    private final AuthService authService;
    private final UserRepository userRepository;

    // Khi khởi tạo, đưa cho nó Giao diện, Dịch vụ Firebase và DB
    public LoginController(LoginView view, AuthService authService, UserRepository userRepository) {
        this.view = view;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // ĐÂY LÀ TOÀN BỘ LOGIC CỦA TRANG LOGIN
    public void performLogin(String email, String password) {
        // 1. Kiểm tra rỗng
        if (email.isEmpty() || password.isEmpty()) {
            view.showError("Vui lòng nhập tài khoản/mật khẩu!");
            return;
        }

        view.showLoading();

        // 2. Logic đăng nhập Admin
        if (email.equals("admin@gmail.com") && password.equals("123456")) {
            User defaultUser = new User("default_id", "Admin User", "admin@gmail.com");
            userRepository.saveUser(defaultUser);

            view.hideLoading();
            view.showSuccess("Đăng nhập mặc định thành công!");
            view.navigateToMain("Admin User", "admin@gmail.com");
            return;
        }

        // 3. Logic đăng nhập Firebase thật
        authService.Login(email, password, new AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                // Thử tải dữ liệu người dùng từ Cloud về Local SQL
                userRepository.downloadUserFromServer(uid, new UserRepository.UserDownloadCallback() {
                    @Override
                    public void onSuccess(User user) {
                        view.hideLoading();
                        if (user != null && user.getEmail() != null) {
                            view.showSuccess("Đăng nhập thành công!");
                            String displayName = (user.getFullName() != null && !user.getFullName().isEmpty()) 
                                    ? user.getFullName() : user.getEmail().split("@")[0];
                            view.navigateToMain(displayName, user.getEmail());
                        } else {
                            // Trường hợp dữ liệu user trên server bị thiếu thông tin
                            onFailure("Dữ liệu người dùng không hợp lệ trên máy chủ");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        // Nếu không thấy trên Cloud (lỗi hiếm), tạo bản ghi tạm ở Local
                        String name = email.split("@")[0];
                        User newUser = new User(uid, name, email);
                        userRepository.saveUser(newUser);

                        view.hideLoading();
                        view.showSuccess("Đăng nhập thành công!");
                        view.navigateToMain(name, email);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                view.hideLoading();
                view.showError("Đăng nhập thất bại: " + error);
            }
        });
    }
}