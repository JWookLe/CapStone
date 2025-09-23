package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_records")
public class EmotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String emotionType;

    private String contextData;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    // 기본 생성자
    public EmotionRecord() {
        this.recordedAt = LocalDateTime.now();
    }

    // 생성자
    public EmotionRecord(User user, String emotionType, String contextData) {
        this.user = user;
        this.emotionType = emotionType;
        this.contextData = contextData;
        this.recordedAt = LocalDateTime.now();
    }

    // Getter 메서드
    public Long getRecordId() {
        return recordId;
    }

    public User getUser() {
        return user;
    }

    public String getEmotionType() {
        return emotionType;
    }

    public String getContextData() {
        return contextData;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    // Setter 메서드
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEmotionType(String emotionType) {
        this.emotionType = emotionType;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
} 