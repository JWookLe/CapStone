package com.example.EmotionSync.model;

public class FriendItem {
    private String userId;
    private String name;
    private String profileImage;

    public FriendItem() {
        // Firebase에서 사용하기 위한 빈 생성자
    }

    public FriendItem(String userId, String name, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNickname() {
        return name;
    }
} 