package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.EmotionRecord;
import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, Long> {
    // 특정 사용자의 모든 감정 기록 조회
    List<EmotionRecord> findByUser(User user);

    // 특정 사용자의 특정 감정 타입 기록 조회
    List<EmotionRecord> findByUserAndEmotionType(User user, String emotionType);

    EmotionRecord findTopByUserOrderByRecordedAtDesc(User user);

    void deleteByUser(User user);
}