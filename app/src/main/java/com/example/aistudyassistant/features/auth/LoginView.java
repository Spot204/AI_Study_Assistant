package com.example.aistudyassistant.features.auth;

public interface LoginView {
    void showLoading(); // Bật vòng xoay
    void hideLoading(); // Tắt vòng xoay
    void showError(String message); // Hiện Toast lỗi
    void showSuccess(String message); // Hiện Toast thành công
    void navigateToMain(String name, String email); // Chuyển trang
}