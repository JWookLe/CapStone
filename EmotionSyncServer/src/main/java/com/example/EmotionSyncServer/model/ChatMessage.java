package com.example.EmotionSyncServer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private ChatRoom chatRoom;

    @ManyToOne
    @JsonIgnore
    private User sender;

    @Column(nullable = false)
    private String content;

    private LocalDateTime timestamp;

    private boolean isRead;

    public ChatMessage(ChatRoom chatRoom, User sender, String content, LocalDateTime timestamp, boolean isRead) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
}
