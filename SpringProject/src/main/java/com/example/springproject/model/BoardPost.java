package com.example.springproject.model;

import java.time.LocalDateTime;

public class BoardPost {
    private Long id;
    private String title;
    private String content;
    private String userId;
    private String maskedUserId;
    private LocalDateTime createdAt;
    private int viewCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMaskedUserId() { return maskedUserId; }
    public void setMaskedUserId(String maskedUserId) { this.maskedUserId = maskedUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
} 