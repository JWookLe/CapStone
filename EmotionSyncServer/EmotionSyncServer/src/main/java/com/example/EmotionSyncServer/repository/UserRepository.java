package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);

    // 이름과 전화번호로 사용자 찾기
    Optional<User> findByNameAndPhone(String name, String phone);
}