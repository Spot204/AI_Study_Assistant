package com.example.aistudyassistant.services;


public interface AuthCallback {
    void onSuccess(String uid);
    void onFailure(String error);
}
