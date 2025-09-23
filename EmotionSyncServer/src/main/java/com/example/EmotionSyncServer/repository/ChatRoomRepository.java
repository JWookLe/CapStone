package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.ChatRoom;
import com.example.EmotionSyncServer.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT r FROM ChatRoom r WHERE (r.user1.id = :id1 AND r.user2.id = :id2) OR (r.user1.id = :id2 AND r.user2.id = :id1)")
    List<ChatRoom> findByParticipants(String id1, String id2);

    @Query("SELECT r FROM ChatRoom r WHERE r.user1 = :user1 OR r.user2 = :user2")
    List<ChatRoom> findByUser1OrUser2(User user1, User user2);

    void deleteByUser1OrUser2(User user1, User user2);

}