package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.ChatMessage;
import com.example.EmotionSyncServer.model.ChatRoom;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatRoom chatRoom, User sender, String content) {
        ChatMessage message = new ChatMessage(
                chatRoom,
                sender,
                content,
                LocalDateTime.now(),
                false
        );
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessages(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
    }
}
