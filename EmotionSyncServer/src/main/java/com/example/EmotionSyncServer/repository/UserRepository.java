package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByNameAndPhone(String name, String phone);
    Optional<User> findByInviteCode(String inviteCode);
}