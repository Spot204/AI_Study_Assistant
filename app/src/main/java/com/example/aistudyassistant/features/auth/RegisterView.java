package com.example.aistudyassistant.features.auth;

public interface RegisterView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
    void navigateToLogin(); // Đăng ký xong thì quay lại trang Login
}