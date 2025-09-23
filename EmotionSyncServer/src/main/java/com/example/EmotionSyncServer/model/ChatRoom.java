package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user1;

    @ManyToOne
    private User user2;

    private LocalDateTime createdAt;

    public ChatRoom(User user1, User user2, LocalDateTime createdAt) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }
}