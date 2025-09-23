package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.ChatRoom;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public void createRoomIfNotExists(User user1, User user2) {
        List<ChatRoom> existing = chatRoomRepository.findByParticipants(user1.getId(), user2.getId());

        if (!existing.isEmpty()) return;

        ChatRoom room = new ChatRoom(user1, user2, LocalDateTime.now());
        chatRoomRepository.save(room);
    }

    public ChatRoom getChatRoom(User user1, User user2) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipants(user1.getId(), user2.getId());
        if (rooms.isEmpty()) {
            throw new RuntimeException("Chat room not found");
        }
        return rooms.get(0);
    }

    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
    }
}