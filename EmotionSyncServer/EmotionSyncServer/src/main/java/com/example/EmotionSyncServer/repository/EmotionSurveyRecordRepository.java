package com.example.EmotionSyncServer.repository;

import com.example.EmotionSyncServer.model.EmotionSurveyRecord;
import com.example.EmotionSyncServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionSurveyRecordRepository extends JpaRepository<EmotionSurveyRecord, Long> {
    // 특정 사용자의 모든 설문 기록 조회
    List<EmotionSurveyRecord> findByUser(User user);
}