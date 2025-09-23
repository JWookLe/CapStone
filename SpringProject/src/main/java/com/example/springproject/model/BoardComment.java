package com.example.springproject.model;

import java.time.LocalDateTime;

public class BoardComment {
    private Long id;
    private Long postId;
    private Long parentId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private int depth; // JSP에서 들여쓰기용
    private String author;
    private String maskedUserId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getMaskedUserId() { return maskedUserId; }
    public void setMaskedUserId(String maskedUserId) { this.maskedUserId = maskedUserId; }
} 