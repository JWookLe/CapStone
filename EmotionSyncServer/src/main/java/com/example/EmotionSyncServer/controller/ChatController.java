package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.dto.ChatMessageDto;
import com.example.EmotionSyncServer.model.ChatMessage;
import com.example.EmotionSyncServer.model.ChatRoom;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.service.ChatMessageService;
import com.example.EmotionSyncServer.service.ChatRoomService;
import com.example.EmotionSyncServer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final UserService userService;

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(@RequestBody Map<String, String> messageRequest) {
        String senderId = messageRequest.get("senderId");
        String receiverId = messageRequest.get("receiverId");
        String content = messageRequest.get("content");

        User sender = userService.findById(senderId);
        User receiver = userService.findById(receiverId);

        // 채팅방이 없으면 생성
        chatRoomService.createRoomIfNotExists(sender, receiver);

        // 메시지 저장
        ChatMessage message = chatMessageService.saveMessage(
            chatRoomService.getChatRoom(sender, receiver),
            sender,
            content
        );

        return ResponseEntity.ok(new ChatMessageDto(message));
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(roomId);
        List<ChatMessage> messages = chatMessageService.getMessages(chatRoom);
        List<ChatMessageDto> messageDtos = messages.stream()
            .map(ChatMessageDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(messageDtos);
    }
} 