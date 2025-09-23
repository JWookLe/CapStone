package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.EmotionRecord;
import com.example.EmotionSyncServer.model.EmotionSurveyRecord;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.EmotionRecordRepository;
import com.example.EmotionSyncServer.repository.EmotionSurveyRecordRepository;
import com.example.EmotionSyncServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmotionSurveyRecordService {

    @Autowired
    private EmotionSurveyRecordRepository emotionSurveyRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmotionRecordRepository emotionRecordRepository;

    /**
     * 감정 설문 기록 저장
     * @param surveyData 설문 데이터를 포함한 Map
     * @return 성공 여부
     */
    @Transactional
    public boolean recordEmotionSurvey(Map<String, Object> surveyData) {
        try {
            String userId = (String) surveyData.get("userId");
            Long recordId = ((Number) surveyData.get("recordId")).longValue();
            int question1 = ((Number) surveyData.get("question1")).intValue();
            int question2 = ((Number) surveyData.get("question2")).intValue();
            int question3 = ((Number) surveyData.get("question3")).intValue();
            int question4 = ((Number) surveyData.get("question4")).intValue();
            String question5 = (String) surveyData.get("question5");

            // 사용자 정보 확인
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return false;
            }

            // 감정 기록 정보 확인
            Optional<EmotionRecord> recordOptional = emotionRecordRepository.findById(recordId);
            if (!recordOptional.isPresent()) {
                return false;
            }

            // 설문 결과 저장
            EmotionSurveyRecord surveyRecord = new EmotionSurveyRecord(
                    userOptional.get(),
                    recordOptional.get(),
                    question1,
                    question2,
                    question3,
                    question4,
                    question5
            );

            emotionSurveyRecordRepository.save(surveyRecord);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 특정 사용자의 모든 설문 기록 조회
     * @param userId 사용자 ID
     * @return 설문 기록 리스트
     */
    public List<EmotionSurveyRecord> getUserSurveyRecords(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            return emotionSurveyRecordRepository.findByUser(userOptional.get());
        }

        return List.of(); // 빈 리스트 반환
    }

    /**
     * 사용자의 최신 설문 기록 조회
     * @param userId 사용자 ID
     * @return 최신 설문 기록 (없으면 null)
     */
    public EmotionSurveyRecord getLatestSurveyRecord(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            return emotionSurveyRecordRepository.findTopByUserOrderBySurveyIdDesc(userOptional.get());
        }

        return null;
    }
}