package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.Friend;
import com.example.EmotionSyncServer.model.FriendStatus;
import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByRequesterAndReceiver(User requester, User receiver);
    boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, FriendStatus status);
    void deleteAllByRequesterOrReceiver(User requester, User receiver);
    List<Friend> findByReceiverAndStatus(User receiver, FriendStatus status);
    Optional<Friend> findByIdAndReceiver(Long id, User receiver);
}