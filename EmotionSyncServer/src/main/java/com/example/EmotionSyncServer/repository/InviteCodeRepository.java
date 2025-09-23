package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.InviteCode;
import com.example.EmotionSyncServer.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
    Optional<InviteCode> findByCode(String code);
    void deleteByUser(User user);
}