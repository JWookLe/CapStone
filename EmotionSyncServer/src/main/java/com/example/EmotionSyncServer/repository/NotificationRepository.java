package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(String receiverId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.receiverId = :receiverId")
    void deleteByReceiverId(@Param("receiverId") String receiverId);
}