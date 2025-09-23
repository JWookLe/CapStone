package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.model.EmotionRecord;
import com.example.EmotionSyncServer.jwt.JwtUtil;
import com.example.EmotionSyncServer.service.EmotionRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotions")
public class EmotionRecordController {

    private static final Logger logger = LoggerFactory.getLogger(EmotionRecordController.class);
    
    @Autowired
    private EmotionRecordService emotionRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 감정 기록 저장 API
     * @param request 감정 기록 요청
     * @param token 인증 토큰
     * @return 감정 기록 결과
     */
    @PostMapping
    public ResponseEntity<?> recordEmotion(@RequestBody Map<String, String> request,
                                         @RequestHeader("Authorization") String token) {
        try {
            // 토큰에서 사용자 ID 추출
            String tokenUserId = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            
            // 요청에서 사용자 ID 추출 (요청에 포함된 경우)
            String requestUserId = request.getOrDefault("userId", tokenUserId);
            
            // 사용자 ID가 토큰의 ID와 일치하는지 확인
            if (!tokenUserId.equals(requestUserId)) {
                logger.error("토큰의 사용자 ID와 요청의 사용자 ID가 일치하지 않습니다: token={}, request={}", tokenUserId, requestUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
            }
            
            // 요청 데이터에 사용자 ID 설정
            request.put("userId", tokenUserId);
            
            // 감정 기록 저장
            EmotionRecord savedRecord = emotionRecordService.recordEmotion(request);
            
            if (savedRecord != null) {
                logger.info("감정 기록 저장 성공: userId={}, emotionType={}", tokenUserId, request.get("emotionType"));
                
                // 성공 응답에 record_id 추가
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "감정이 성공적으로 기록되었습니다.");
                response.put("record_id", savedRecord.getRecordId());
                
                return ResponseEntity.ok(response);
            } else {
                logger.error("감정 기록 저장 실패: userId={}, emotionType={}", tokenUserId, request.get("emotionType"));
                return ResponseEntity.badRequest().body("감정 기록에 실패했습니다.");
            }
        } catch (Exception e) {
            logger.error("감정 기록 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자의 감정 기록 조회 API
     * @param userId 조회할 사용자 ID
     * @param token 인증 토큰
     * @return 감정 기록 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserEmotionRecords(
            @PathVariable String userId,
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 토큰에서 사용자 ID 추출
            String authToken = token.replace("Bearer ", "");
            String tokenUserId = jwtUtil.extractUsername(authToken);
            
            // 토큰의 사용자 ID와 요청 경로의 사용자 ID가 일치하는지 확인
            if (!tokenUserId.equals(userId)) {
                logger.warn("사용자 ID 불일치: 토큰 ID={}, 요청 ID={}", tokenUserId, userId);
                response.put("success", false);
                response.put("message", "본인의 기록만 조회할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // 사용자의 감정 기록 조회
            logger.info("감정 기록 조회 요청: 사용자={}", userId);
            List<EmotionRecord> records = emotionRecordService.getUserEmotionRecords(userId);
            
            response.put("success", true);
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("감정 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 사용자의 특정 감정 유형 기록 조회 API
     * @param userId 조회할 사용자 ID
     * @param emotionType 감정 유형
     * @param token 인증 토큰
     * @return 감정 기록 목록
     */
    @GetMapping("/user/{userId}/type/{emotionType}")
    public ResponseEntity<Map<String, Object>> getUserEmotionRecordsByType(
            @PathVariable String userId,
            @PathVariable String emotionType,
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 토큰에서 사용자 ID 추출
            String authToken = token.replace("Bearer ", "");
            String tokenUserId = jwtUtil.extractUsername(authToken);
            
            // 토큰의 사용자 ID와 요청 경로의 사용자 ID가 일치하는지 확인
            if (!tokenUserId.equals(userId)) {
                logger.warn("사용자 ID 불일치: 토큰 ID={}, 요청 ID={}", tokenUserId, userId);
                response.put("success", false);
                response.put("message", "본인의 기록만 조회할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // 사용자의 특정 감정 유형 기록 조회
            logger.info("감정 유형별 기록 조회 요청: 사용자={}, 감정={}", userId, emotionType);
            List<EmotionRecord> records = emotionRecordService.getUserEmotionRecordsByType(userId, emotionType);
            
            response.put("success", true);
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("감정 유형별 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 최신 감정 기록 조회 API
     * @param userId 조회할 사용자 ID
     * @return 최신 감정 기록
     */
    @GetMapping("/latest/{userId}")
    public ResponseEntity<Map<String, Object>> getLatestEmotionRecord(
            @PathVariable String userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("최신 감정 기록 조회 요청: 사용자={}", userId);
            EmotionRecord latestRecord = emotionRecordService.getLatestEmotionRecord(userId);
            
            if (latestRecord != null) {
                response.put("success", true);
                response.put("record", latestRecord);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("최신 감정 기록 없음: 사용자={}", userId);
                response.put("success", false);
                response.put("message", "최신 감정 기록이 없습니다.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("최신 감정 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 