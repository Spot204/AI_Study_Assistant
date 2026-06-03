package com.example.aistudyassistant.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String userId;
    private String fullName;
    private String email;
    private String bio;
    private String school;
    private String avatarPath;

    public User(@NonNull String userId, String fullName, String email, String bio, String school) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.bio = bio;
        this.school = school;
    }

    @androidx.room.Ignore
    public User(@NonNull String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
}
