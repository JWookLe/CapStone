package com.example.EmotionSync.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ChatMessageEntity message);

    @Update
    void update(ChatMessageEntity message);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp DESC LIMIT :limit")
    List<ChatMessageEntity> getMessages(String userId1, String userId2, int limit);

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    int getUnreadCount(String userId);

    @Query("UPDATE chat_messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId")
    void markMessagesAsRead(String userId, String senderId);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    List<ChatMessageEntity> getMessagesBetweenUsers(String userId1, String userId2);

    @Query("SELECT * FROM chat_messages WHERE receiverId = :currentUserId AND isRead = 0 ORDER BY timestamp ASC")
    List<ChatMessageEntity> getAllUnreadMessages(String currentUserId);

    @Query("SELECT * FROM chat_messages WHERE receiverId = :receiverId AND senderId = :senderId AND isRead = 0 ORDER BY timestamp ASC")
    List<ChatMessageEntity> getUnreadMessages(String receiverId, String senderId);

    @Update
    void updateMessages(List<ChatMessageEntity> messages);

} 