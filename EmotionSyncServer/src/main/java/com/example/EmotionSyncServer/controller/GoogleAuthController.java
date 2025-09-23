package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> authenticateGoogle(@RequestBody Map<String, String> tokenData) {
        System.out.println("✅ [서버] /api/auth/google 요청 받음: " + tokenData);
        String idToken = tokenData.get("idToken");
        return ResponseEntity.ok(googleAuthService.authenticateGoogleUser(idToken));
    }

}
