package com.example.EmotionSyncServer.websocket;

import com.example.EmotionSyncServer.model.ChatMessage;
import com.example.EmotionSyncServer.model.ChatRoom;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.ChatRoomRepository;
import com.example.EmotionSyncServer.repository.UserRepository;
import com.example.EmotionSyncServer.service.ChatMessageService;
import com.example.EmotionSyncServer.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final NotificationService notificationService;
    // 현재 접속 중인 유저 세션 관리
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        sessions.put(userId, session);
        logger.info("🔌 새로운 연결 - User ID: {}", userId);
        logger.info("📡 현재 세션 키: {}", sessions.keySet());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("📩 수신한 원본 메시지: {}", message.getPayload());
        logger.info("📡 현재 세션 키: {}", sessions.keySet());

        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);

        // ✅ 정규화 처리
        String senderId = normalizeUserId(payload.get("senderId").trim());
        String receiverId = normalizeUserId(payload.get("receiverId").trim());
        String content = payload.get("content");
        String type = payload.get("type");

        if ("SHARE".equals(type)) {
            handleShareMessage(senderId, receiverId, content);
        } else {
            handleChatMessage(senderId, receiverId, content);
        }
    }

    private void handleShareMessage(String senderId, String receiverId, String content) {
        try {
            Optional<User> senderOpt = userRepository.findById(senderId);
            Optional<User> receiverOpt = userRepository.findById(receiverId);
            if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
                logger.warn("❌ 유저 조회 실패 - senderId: {}, receiverId: {}", senderId, receiverId);
                return;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("type", "SHARE");
            response.put("senderId", senderId);
            response.put("receiverId", receiverId);
            response.put("content", content);
            response.put("timestamp", System.currentTimeMillis());

            String jsonResponse = objectMapper.writeValueAsString(response);

            // 보내는 사람에게 전송
            if (sessions.containsKey(senderId)) {
                logger.info("📤 송신자({})에게 공유 메시지 전송", senderId);
                sessions.get(senderId).sendMessage(new TextMessage(jsonResponse));
            } else {
                logger.warn("❗ 송신자 세션 없음: {}", senderId);
            }

            // 받는 사람에게 전송
            if (sessions.containsKey(receiverId)) {
                logger.info("📤 수신자({})에게 공유 메시지 전송", receiverId);
                sessions.get(receiverId).sendMessage(new TextMessage(jsonResponse));
            } else {
                logger.warn("❗ 수신자 세션 없음: {}", receiverId);
            }
        } catch (Exception e) {
            logger.error("공유 메시지 처리 중 오류 발생", e);
        }
    }

    private void handleChatMessage(String senderId, String receiverId, String content) {
        try {
            logger.info("📨 메시지 도착 - From: {}, To: {}, Content: {}", senderId, receiverId, content);

            Optional<User> senderOpt = userRepository.findById(senderId);
            if (senderOpt.isPresent()) {
                String senderName = senderOpt.get().getName();  // 또는 getUsername()
                notificationService.sendNotification(
                        receiverId,
                        "메시지",
                        senderName + "님이 메시지를 보냈습니다",
                        senderId
                );
            }

            // 메시지 생성
            ObjectNode chatMessage = objectMapper.createObjectNode();
            chatMessage.put("type", "CHAT");
            chatMessage.put("senderId", senderId);
            chatMessage.put("content", content);
            chatMessage.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString());

            // 수신자의 세션 찾기
            WebSocketSession receiverSession = sessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(chatMessage.toString()));
            } else {
                logger.warn("❗ 수신자 세션 없음: {}", receiverId);
            }
        } catch (Exception e) {
            logger.error("채팅 메시지 처리 중 오류 발생", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        sessions.remove(userId);
        logger.info("🔌 연결 종료 - User ID: {}", userId);
    }

    private String getUserIdFromSession(WebSocketSession session) {
        return session.getUri().getQuery().split("=")[1];
    }

    private String normalizeUserId(String userId) {
        // 실제 구현에 따라 정규화 로직을 구현해야 합니다.
        // 여기서는 간단하게 앞뒤 공백을 제거합니다.
        return userId.trim();
    }
}
