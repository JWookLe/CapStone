package com.example.EmotionSyncServer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    private String receiverId; // 알림 받는 사람

    private String senderId;

    private String type; // "FRIEND_REQUEST", "FRIEND_ACCEPTED"

    private String content; // "A님이 친구 요청을 보냈습니다" 등

    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
