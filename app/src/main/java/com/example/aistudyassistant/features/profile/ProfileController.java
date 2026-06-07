package com.example.aistudyassistant.features.profile;

public class ProfileController {
    private final UserRepository repository;
    private final SyncService syncService;

    public ProfileController(UserRepository repo, SyncService sync) {
        this.repository = repo;
        this.syncService = sync;
    }

    public void updateProfile(UserEntity user) {
        // Lưu local để app phản hồi nhanh
        repository.updateUser(user);

        // Đồng bộ lên Cloud
        syncService.syncUserProfile(user);
    }
}
