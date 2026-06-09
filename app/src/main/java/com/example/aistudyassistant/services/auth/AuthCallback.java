package com.example.aistudyassistant.services.auth;


public interface AuthCallback {
    void onSuccess(String uid);
    void onFailure(String error);
}
