package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.entity.Notification;
import com.example.EmotionSyncServer.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(String receiverId, String type, String content, String senderId) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    public void markAsRead(String notificationId) {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림이 존재하지 않습니다."));
        noti.setIsRead(true);
        notificationRepository.save(noti);
    }
}