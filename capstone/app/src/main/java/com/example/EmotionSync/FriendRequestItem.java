package com.example.EmotionSync;

public class FriendRequestItem {
    private Long id;
    private String requesterId;

    public FriendRequestItem(Long id, String requesterId) {
        this.id = id;
        this.requesterId = requesterId;
    }

    public Long getId() {
        return id;
    }

    public String getRequesterId() {
        return requesterId;
    }
}