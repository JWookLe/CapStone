package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.jwt.JwtUtil;
import com.example.EmotionSyncServer.model.EmotionSurveyRecord;
import com.example.EmotionSyncServer.service.EmotionSurveyRecordService;
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
@RequestMapping("/api/surveys")
public class EmotionSurveyRecordController {

    private static final Logger logger = LoggerFactory.getLogger(EmotionSurveyRecordController.class);

    @Autowired
    private EmotionSurveyRecordService surveyRecordService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 감정 설문 기록 저장 API
     * @param surveyData 설문 데이터
     * @param token 인증 토큰
     * @return 응답 결과
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordEmotionSurvey(
            @RequestBody Map<String, Object> surveyData,
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 토큰에서 사용자 ID 추출
            String authToken = token.replace("Bearer ", "");
            String userId = jwtUtil.extractUsername(authToken);

            // 토큰의 사용자 ID와 요청 데이터의 사용자 ID가 일치하는지 확인
            if (!userId.equals(surveyData.get("userId"))) {
                logger.warn("사용자 ID 불일치: 토큰 ID={}, 요청 ID={}", userId, surveyData.get("userId"));
                response.put("success", false);
                response.put("message", "인증 정보가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 설문 기록 저장
            logger.info("설문 기록 요청: 사용자={}", userId);
            boolean result = surveyRecordService.recordEmotionSurvey(surveyData);

            if (result) {
                logger.info("설문 기록 성공: 사용자={}", userId);
                response.put("success", true);
                response.put("message", "설문이 성공적으로 기록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("설문 기록 실패: 사용자={}", userId);
                response.put("success", false);
                response.put("message", "설문 기록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("설문 기록 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 설문 기록 조회 API
     * @param userId 조회할 사용자 ID
     * @param token 인증 토큰
     * @return 설문 기록 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSurveyRecords(
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
                response.put("message", "본인의 설문 기록만 조회할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 사용자의 설문 기록 조회
            logger.info("설문 기록 조회 요청: 사용자={}", userId);
            List<EmotionSurveyRecord> records = surveyRecordService.getUserSurveyRecords(userId);

            response.put("success", true);
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("설문 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 최신 설문 기록 조회 API
     * @param userId 조회할 사용자 ID
     * @return 최신 설문 기록
     */
    @GetMapping("/latest/{userId}")
    public ResponseEntity<Map<String, Object>> getLatestSurveyRecord(
            @PathVariable String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("최신 설문 기록 조회 요청: 사용자={}", userId);
            EmotionSurveyRecord latestRecord = surveyRecordService.getLatestSurveyRecord(userId);

            if (latestRecord != null) {
                response.put("success", true);
                response.put("record", latestRecord);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("최신 설문 기록 없음: 사용자={}", userId);
                response.put("success", false);
                response.put("message", "최신 설문 기록이 없습니다.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("최신 설문 기록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}