package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.model.UserPreferenceMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserPreferenceMatchRepository extends JpaRepository<UserPreferenceMatch, Long> {
    @Query("SELECT m FROM UserPreferenceMatch m WHERE (m.user1 = :userA AND m.user2 = :userB) OR (m.user1 = :userB AND m.user2 = :userA)")
    Optional<UserPreferenceMatch> findByUsers(@Param("userA") User userA, @Param("userB") User userB);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPreferenceMatch m WHERE m.user1 = :user1 OR m.user2 = :user2")
    void deleteByUser1OrUser2(User user1, User user2);
} 