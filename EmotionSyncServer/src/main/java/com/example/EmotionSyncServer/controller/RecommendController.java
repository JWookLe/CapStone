package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> recommendContent(
            @RequestParam String emotion,
            @RequestParam int q1,
            @RequestParam int q2,
            @RequestParam int q3,
            @RequestParam int q4,
            @RequestParam String q5,
            @RequestHeader("Authorization") String token
    ) {
        try {
            String authToken = token.replace("Bearer ", "");

            if (!jwtUtil.validateToken(authToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }

            String userId = jwtUtil.extractUsername(authToken);
            System.out.println("✅ 추천 요청한 사용자: " + userId);

            // Flask 서버 URL 구성 (쿼리 추가!)
            String flaskUrl = UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:5000/recommend")
                    .queryParam("emotion", emotion)
                    .queryParam("q1", q1)
                    .queryParam("q2", q2)
                    .queryParam("q3", q3)
                    .queryParam("q4", q4)
                    .queryParam("q5", q5)
                    .queryParam("user_id", userId)
                    .build()
                    .toUriString();

            ResponseEntity<Object> response = restTemplate.getForEntity(flaskUrl, Object.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("추천 처리 중 서버 오류가 발생했습니다.");
        }
    }

}