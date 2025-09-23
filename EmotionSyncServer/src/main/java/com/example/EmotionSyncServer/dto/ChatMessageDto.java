package com.example.EmotionSyncServer.dto;

import com.example.EmotionSyncServer.model.ChatMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public ChatMessageDto(ChatMessage message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
        this.isRead = message.isRead();
    }
} 