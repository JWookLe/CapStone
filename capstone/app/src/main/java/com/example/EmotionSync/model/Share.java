package com.example.EmotionSync.model;

import com.google.gson.annotations.SerializedName;

public class Share {
    private Long id;
    
    @SerializedName("user1_id")
    private String user1Id;
    
    @SerializedName("user2_id")
    private String user2Id;
    
    @SerializedName("contentURL")
    private String contentUrl;
    
    @SerializedName("is_liked")
    private boolean isLiked;
    
    @SerializedName("is_disliked")
    private boolean isDisliked;

    public Share() {
        // Firebase에서 사용하기 위한 빈 생성자
    }

    public Share(Long id, String user1Id, String user2Id, String contentUrl, boolean isLiked, boolean isDisliked) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.contentUrl = contentUrl;
        this.isLiked = isLiked;
        this.isDisliked = isDisliked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        // 카카오 계정인 경우 ID 형식 수정
        if (user1Id != null && user1Id.startsWith("kakao_")) {
            this.user1Id = user1Id;
        } else if (user1Id != null && user1Id.matches("\\d+")) {
            // 숫자로만 구성된 ID는 카카오 계정으로 가정하지만, 
            // 구글 계정일 수도 있으므로 google_ 접두사 사용
            this.user1Id = "google_" + user1Id + "@google.local";
        } else {
            this.user1Id = user1Id;
        }
    }

    // 구글 계정을 위한 별도 setter (카카오 변환 로직 우회)
    public void setUser1IdDirect(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isDisliked() {
        return isDisliked;
    }

    public void setDisliked(boolean disliked) {
        isDisliked = disliked;
    }
} 