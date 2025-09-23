package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.dto.ShareDto;
import com.example.EmotionSyncServer.dto.MatchRateDto;
import com.example.EmotionSyncServer.model.Share;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.service.ShareService;
import com.example.EmotionSyncServer.service.UserService;
import com.example.EmotionSyncServer.service.UserPreferenceMatchService;
import com.example.EmotionSyncServer.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserPreferenceMatchService matchService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/share")
    public ResponseEntity<ShareDto> shareContent(@RequestBody ShareDto dto, @RequestHeader("Authorization") String authHeader) {
        System.out.println("🔍 공유 요청:");
        System.out.println("  - 원본 user1Id: " + dto.getUser1Id());
        System.out.println("  - 원본 user2Id: " + dto.getUser2Id());
        
        // JWT 토큰에서 사용자 이메일 추출
        String userEmail = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                userEmail = jwtUtil.extractUsername(token);
                System.out.println("  - JWT에서 추출한 이메일: " + userEmail);
            } catch (Exception e) {
                System.out.println("❌ JWT 토큰 파싱 실패: " + e.getMessage());
            }
        }
        
        // 공유자 ID 결정 (JWT에서 추출한 이메일 우선 사용)
        String actualUser1Id = userEmail != null ? userEmail : dto.getUser1Id();
        
        // 사용자 ID 정규화
        String normalizedUser1Id = normalizeUserId(actualUser1Id);
        String normalizedUser2Id = normalizeUserId(dto.getUser2Id());
        
        System.out.println("  - 정규화된 user1Id: " + normalizedUser1Id);
        System.out.println("  - 정규화된 user2Id: " + normalizedUser2Id);
        
        User user1 = userService.findById(normalizedUser1Id);
        if (user1 == null) {
            System.out.println("❌ 공유하는 사용자를 찾을 수 없음: " + normalizedUser1Id);
            throw new RuntimeException("공유하는 사용자를 찾을 수 없습니다.");
        }

        User user2 = userService.findById(normalizedUser2Id);
        if (user2 == null) {
            System.out.println("❌ 공유받는 사용자를 찾을 수 없음: " + normalizedUser2Id);
            throw new RuntimeException("공유받는 사용자를 찾을 수 없습니다.");
        }

        System.out.println("✅ 사용자 조회 성공:");
        System.out.println("  - 공유자: " + user1.getId() + " (" + user1.getName() + ")");
        System.out.println("  - 수신자: " + user2.getId() + " (" + user2.getName() + ")");

        Share share = shareService.shareContent(user1, user2, dto.getContentURL());

        ShareDto responseDto = new ShareDto();
        responseDto.setId(share.getId());
        responseDto.setUser1Id(share.getUser1().getId());
        responseDto.setUser2Id(share.getUser2().getId());
        responseDto.setContentURL(share.getContentURL());
        responseDto.setLiked(share.isLiked());
        responseDto.setDisliked(share.isDisliked());

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<List<ShareDto>> getReceivedShares(@PathVariable String userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        List<Share> shares = shareService.getReceivedShares(user);
        List<ShareDto> dtos = shares.stream()
                .map(share -> {
                    ShareDto dto = new ShareDto();
                    dto.setId(share.getId());
                    dto.setUser1Id(share.getUser1().getId());
                    dto.setUser2Id(share.getUser2().getId());
                    dto.setContentURL(share.getContentURL());
                    dto.setLiked(share.isLiked());
                    dto.setDisliked(share.isDisliked());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<ShareDto>> getSentShares(@PathVariable String userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        List<Share> shares = shareService.getSentShares(user);
        List<ShareDto> dtos = shares.stream()
                .map(share -> {
                    ShareDto dto = new ShareDto();
                    dto.setId(share.getId());
                    dto.setUser1Id(share.getUser1().getId());
                    dto.setUser2Id(share.getUser2().getId());
                    dto.setContentURL(share.getContentURL());
                    dto.setLiked(share.isLiked());
                    dto.setDisliked(share.isDisliked());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{shareId}/like")
    public ResponseEntity<ShareDto> likeShare(@PathVariable Long shareId) {
        Share share = shareService.likeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{shareId}/dislike")
    public ResponseEntity<ShareDto> dislikeShare(@PathVariable Long shareId) {
        Share share = shareService.dislikeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{shareId}/like")
    public ResponseEntity<ShareDto> cancelLikeShare(@PathVariable Long shareId) {
        Share share = shareService.cancelLikeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{shareId}/dislike")
    public ResponseEntity<ShareDto> cancelDislikeShare(@PathVariable Long shareId) {
        Share share = shareService.cancelDislikeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{shareId}/like/cancel")
    public ResponseEntity<ShareDto> cancelLikeSharePost(@PathVariable Long shareId) {
        Share share = shareService.cancelLikeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{shareId}/dislike/cancel")
    public ResponseEntity<ShareDto> cancelDislikeSharePost(@PathVariable Long shareId) {
        Share share = shareService.cancelDislikeShare(shareId);
        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<?> getShare(
            @RequestParam("sharedBy") String sharedBy,
            @RequestParam("sharedTo") String sharedTo,
            @RequestParam("contentURL") String contentURL) {

        System.out.println("🔍 공유 정보 조회 요청:");
        System.out.println("  - 수신자 ID: " + sharedTo);
        System.out.println("  - 컨텐츠 URL: " + contentURL);

        // 수신자만 확인
        User user2 = userService.findById(sharedTo);
        if (user2 == null) {
            System.out.println("❌ 수신자 정보를 찾을 수 없음: " + sharedTo);
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "사용자를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 수신자와 컨텐츠 URL로 공유 정보 검색
        List<Share> shares = shareService.getSharedContentsByUserAndContent(user2, contentURL);
        if (shares.isEmpty()) {
            System.out.println("❌ 공유 정보를 찾을 수 없음:");
            System.out.println("  - 수신자: " + user2.getId());
            System.out.println("  - 컨텐츠: " + contentURL);

            // 사용자의 모든 공유 정보 확인
            List<Share> allShares = shareService.getAllSharesByUser(user2);
            if (!allShares.isEmpty()) {
                System.out.println("ℹ️ 사용자의 다른 공유 정보:");
                for (Share share : allShares) {
                    System.out.println("  - 공유 정보:");
                    System.out.println("    * ID: " + share.getId());
                    System.out.println("    * 공유자: " + share.getUser1().getId());
                    System.out.println("    * 수신자: " + share.getUser2().getId());
                    System.out.println("    * 컨텐츠: " + share.getContentURL());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "공유 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 가장 최근 공유 정보 반환
        Share share = shares.get(0);
        System.out.println("✅ 공유 정보 조회 성공:");
        System.out.println("  - 공유 ID: " + share.getId());
        System.out.println("  - 공유자: " + share.getUser1().getId());
        System.out.println("  - 수신자: " + share.getUser2().getId());
        System.out.println("  - 컨텐츠: " + share.getContentURL());

        ShareDto dto = new ShareDto();
        dto.setId(share.getId());
        dto.setUser1Id(share.getUser1().getId());
        dto.setUser2Id(share.getUser2().getId());
        dto.setContentURL(share.getContentURL());
        dto.setLiked(share.isLiked());
        dto.setDisliked(share.isDisliked());

        return ResponseEntity.ok(dto);
    }

    private String normalizeUserId(String userId) {
        if (userId == null) {
            return null;
        }
        
        // 이미 이메일 형식이면 그대로 반환 (구글 계정)
        if (userId.contains("@")) {
            return userId;
        }
        
        // 숫자만 있으면 카카오 계정으로 변환
        if (userId.matches("\\d+")) {
            return "kakao_" + userId + "@kakao.local";
        }
        
        // 그 외의 경우 (카카오 닉네임 등)는 그대로 반환
        return userId;
    }

    @GetMapping("/match-rate")
    public ResponseEntity<?> getMatchRate(
            @RequestParam("user1Id") String user1Id,
            @RequestParam("user2Id") String user2Id) {

        String decodedUser1Id = URLDecoder.decode(user1Id, StandardCharsets.UTF_8);
        String decodedUser2Id = URLDecoder.decode(user2Id, StandardCharsets.UTF_8);

        String finalUser1Id = normalizeUserId(decodedUser1Id);
        String finalUser2Id = normalizeUserId(decodedUser2Id);

        System.out.println("user1Id = [" + finalUser1Id + "], user2Id = [" + finalUser2Id + "]");

        User user1 = userService.findById(finalUser1Id);
        User user2 = userService.findById(finalUser2Id);
        if (user1 == null || user2 == null) {
            System.out.println("User not found! user1: " + finalUser1Id + ", user2: " + finalUser2Id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }
        
        int rate = matchService.getMatchRate(user1, user2);
        boolean currentLikeState = matchService.getCurrentLikeState(user1, user2);
        boolean currentDislikeState = matchService.getCurrentDislikeState(user1, user2);
        
        MatchRateDto response = new MatchRateDto();
        response.setMatchRate(rate);
        response.setCurrentLikeState(currentLikeState);
        response.setCurrentDislikeState(currentDislikeState);
        
        return ResponseEntity.ok(response);
    }
}