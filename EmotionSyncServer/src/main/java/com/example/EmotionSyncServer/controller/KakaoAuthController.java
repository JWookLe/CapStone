package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.service.KakaoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    @Autowired
    private KakaoAuthService kakaoAuthService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> authenticateKakao(@RequestBody Map<String, String> tokenData) {
        String kakaoToken = tokenData.get("kakaoToken");
        return ResponseEntity.ok(kakaoAuthService.authenticateKakaoUser(kakaoToken));
    }
}
