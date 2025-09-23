package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.model.EmotionRecord;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.EmotionRecordRepository;
import com.example.EmotionSyncServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmotionRecordService {

    @Autowired
    private EmotionRecordRepository emotionRecordRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 감정 기록 저장
     * @param userId 사용자 ID
     * @param emotionType 감정 유형 (기쁨, 슬픔, 화남, 불안)
     * @param contextData 컨텍스트 데이터 (선택적)
     * @return 저장된 EmotionRecord 객체(실패 시 null)
     */
    @Transactional
    public EmotionRecord recordEmotion(String userId, String emotionType, String contextData) {
        // 사용자 조회
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 감정 유형 검증
            if (!isValidEmotionType(emotionType)) {
                return null;
            }
            
            // 감정 기록 생성 및 저장
            EmotionRecord emotionRecord = new EmotionRecord(user, emotionType, contextData);
            emotionRecord = emotionRecordRepository.save(emotionRecord);
            
            return emotionRecord;
        }
        
        return null;
    }
    
    /**
     * 감정 기록 저장 (Map 파라미터 버전)
     * @param emotionData 감정 데이터를 포함한 Map
     * @return 저장된 EmotionRecord 객체(실패 시 null)
     */
    @Transactional
    public EmotionRecord recordEmotion(Map<String, String> emotionData) {
        String userId = emotionData.get("userId");
        String emotionType = emotionData.get("emotionType");
        String contextData = emotionData.get("contextData");
        
        return recordEmotion(userId, emotionType, contextData);
    }
    
    /**
     * 특정 사용자의 모든 감정 기록 조회
     * @param userId 사용자 ID
     * @return 감정 기록 리스트
     */
    public List<EmotionRecord> getUserEmotionRecords(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            return emotionRecordRepository.findByUser(userOptional.get());
        }
        
        return List.of(); // 빈 리스트 반환
    }
    
    /**
     * 특정 사용자의 특정 감정 타입 기록 조회
     * @param userId 사용자 ID
     * @param emotionType 감정 유형
     * @return 감정 기록 리스트
     */
    public List<EmotionRecord> getUserEmotionRecordsByType(String userId, String emotionType) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent() && isValidEmotionType(emotionType)) {
            return emotionRecordRepository.findByUserAndEmotionType(userOptional.get(), emotionType);
        }
        
        return List.of(); // 빈 리스트 반환
    }
    
    /**
     * 감정 유형이 유효한지 확인
     * @param emotionType 감정 유형
     * @return 유효 여부
     */
    private boolean isValidEmotionType(String emotionType) {
        return emotionType != null && (
            emotionType.equals("기쁨") || 
            emotionType.equals("슬픔") || 
            emotionType.equals("화남") || 
            emotionType.equals("불안")
        );
    }
} 