package com.example.EmotionSyncServer.controller;

import com.example.EmotionSyncServer.jwt.JwtUtil;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;


    // 인증번호 저장용 맵 (사용자 전화번호 -> 인증번호)
    private final Map<String, String> verificationCodes = new HashMap<>();
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> userInfo) {
        Map<String, Object> response = new HashMap<>();

        logger.info("회원가입 요청: {}", userInfo.get("id"));
        if (userService.registerUser(userInfo)) {
            logger.info("회원가입 성공: {}", userInfo.get("id"));
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("회원가입 실패: 이미 사용 중인 아이디");
            response.put("success", false);
            response.put("message", "이미 사용 중인 아이디입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginInfo) {
        Map<String, Object> response = new HashMap<>();

        String id = loginInfo.get("id");
        String password = loginInfo.get("password");

        logger.info("로그인 요청: {}", loginInfo.get("id"));
        if (userService.authenticateUser(loginInfo.get("id"), loginInfo.get("password"))) {
            logger.info("로그인 성공: {}", loginInfo.get("id"));

            String token = jwtUtil.generateToken(id);

            response.put("success", true);
            response.put("message", "로그인 성공!");
            response.put("token", token);

            // ✅ 추가: provider도 내려줌
            Optional<User> userOptional = userService.findUserById(id);
            userOptional.ifPresent(user -> response.put("provider", user.getProvider()));

            return ResponseEntity.ok(response);
        } else {
            logger.warn("로그인 실패: 아이디 또는 비밀번호 불일치");
            response.put("success", false);
            response.put("message", "로그인 실패! 다시 확인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // JWT 토큰 유효성 및 사용자 존재 여부 검증
    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.replace("Bearer", "").trim();
            // 1. 토큰 유효성 검사
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "유효하지 않은 토큰입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            // 2. 토큰에서 사용자 ID 추출
            String userId = jwtUtil.extractUsername(token);
            // 3. 사용자 존재 여부 확인
            Optional<User> userOptional = userService.findUserById(userId);
            if (!userOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "사용자가 존재하지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            // 모두 통과
            response.put("success", true);
            response.put("message", "토큰과 사용자 모두 유효합니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "토큰 검증 중 오류 발생");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    // 아이디 찾기를 위한 인증번호 발송
    @PostMapping("/find-id/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerificationForFindId(@RequestBody Map<String, String> userInfo) {
        Map<String, Object> response = new HashMap<>();
        String name = userInfo.get("name");
        String phone = userInfo.get("phone");

        logger.info("아이디 찾기 인증번호 요청: 이름={}, 전화번호={}", name, phone);

        // 이름과 전화번호로 사용자 확인
        if (userService.isUserExists(name, phone)) {
            // 인증번호 생성 및 저장
            String verificationCode = generateVerificationCode();
            verificationCodes.put(phone, verificationCode);

            // 실제 구현에서는 SMS 서비스를 통해 인증번호 전송
            // 여기서는 로그로만 출력
            logger.info("인증번호 생성: 전화번호={}, 코드={}", phone, verificationCode);

            response.put("success", true);
            response.put("message", "인증번호가 전송되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("사용자 정보 없음: 이름={}, 전화번호={}", name, phone);
            response.put("success", false);
            response.put("message", "해당 정보로 등록된 사용자가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 인증번호 확인 및 아이디 찾기
    @PostMapping("/find-id/verify")
    public ResponseEntity<Map<String, Object>> verifyAndFindId(@RequestBody Map<String, String> verificationInfo) {
        Map<String, Object> response = new HashMap<>();
        String name = verificationInfo.get("name");
        String phone = verificationInfo.get("phone");
        String code = verificationInfo.get("code");

        logger.info("아이디 찾기 인증번호 확인: 전화번호={}, 코드={}", phone, code);

        // 인증번호 검증
        if (verificationCodes.containsKey(phone) && verificationCodes.get(phone).equals(code)) {
            // 인증 성공, 아이디 찾기
            String userId = userService.findUserId(name, phone);
            if (userId != null) {
                response.put("success", true);
                response.put("userId", userId);
                // 사용한 인증번호 제거
                verificationCodes.remove(phone);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } else {
            logger.warn("인증번호 불일치: 전화번호={}", phone);
            response.put("success", false);
            response.put("message", "인증번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 비밀번호 찾기를 위한 인증번호 발송
    @PostMapping("/find-password/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerificationForFindPassword(@RequestBody Map<String, String> userInfo) {
        Map<String, Object> response = new HashMap<>();
        String id = userInfo.get("id");
        String name = userInfo.get("name");
        String phone = userInfo.get("phone");

        logger.info("비밀번호 찾기 인증번호 요청: 아이디={}, 이름={}, 전화번호={}", id, name, phone);

        // 아이디, 이름, 전화번호로 사용자 확인
        if (userService.isUserExists(id, name, phone)) {
            // 인증번호 생성 및 저장
            String verificationCode = generateVerificationCode();
            verificationCodes.put(id, verificationCode);

            // 실제 구현에서는 SMS 서비스를 통해 인증번호 전송
            logger.info("인증번호 생성: 아이디={}, 코드={}", id, verificationCode);

            response.put("success", true);
            response.put("message", "인증번호가 전송되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("사용자 정보 없음: 아이디={}, 이름={}, 전화번호={}", id, name, phone);
            response.put("success", false);
            response.put("message", "해당 정보로 등록된 사용자가 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 비밀번호 찾기 인증번호 확인
    @PostMapping("/find-password/verify")
    public ResponseEntity<Map<String, Object>> verifyForFindPassword(@RequestBody Map<String, String> verificationInfo) {
        Map<String, Object> response = new HashMap<>();
        String id = verificationInfo.get("id");
        String code = verificationInfo.get("code");

        logger.info("비밀번호 찾기 인증번호 확인: 아이디={}, 코드={}", id, code);

        // 인증번호 검증
        if (verificationCodes.containsKey(id) && verificationCodes.get(id).equals(code)) {
            // 인증 성공
            response.put("success", true);
            response.put("message", "인증이 완료되었습니다.");
            // 인증 완료 후 바로 제거하지 않고, 비밀번호 재설정 시까지 유지
            return ResponseEntity.ok(response);
        } else {
            logger.warn("인증번호 불일치: 아이디={}", id);
            response.put("success", false);
            response.put("message", "인증번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> passwordInfo) {
        Map<String, Object> response = new HashMap<>();
        String id = passwordInfo.get("id");
        String newPassword = passwordInfo.get("newPassword");

        logger.info("비밀번호 재설정 요청: 아이디={}", id);

        try {
            if (userService.resetPassword(id, newPassword)) {
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
                verificationCodes.remove(id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("비밀번호 재설정 실패: 아이디={}", id);
                response.put("success", false);
                response.put("message", "비밀번호 재설정에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalStateException e) {
            logger.warn("소셜 로그인 사용자는 비밀번호 재설정 불가: 아이디={}", id);
            response.put("success", false);
            response.put("message", "소셜 로그인 사용자는 비밀번호를 재설정할 수 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("비밀번호 재설정 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 비밀번호 재설정 시 기존 비밀번호 동일 여부 확인
    @PostMapping("/check-password-same")
    public ResponseEntity<Map<String, Object>> checkPasswordSame(@RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        String id = request.get("id");
        String password = request.get("password");

        if (id == null || password == null) {
            response.put("success", false);
            response.put("message", "ID와 비밀번호를 모두 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isSame = userService.verifyUserPassword(id, password);

        response.put("success", true);
        response.put("match", isSame);
        return ResponseEntity.ok(response);
    }


    // 비밀번호 확인 (로그인한 사용자)
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestBody Map<String, String> passwordData,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String authToken = token.replace("Bearer ", "").trim();
            String userId = jwtUtil.extractUsername(authToken);
            String password = passwordData.get("password");

            logger.info("비밀번호 확인 요청: 사용자={}", userId);
            boolean verified = userService.verifyUserPassword(userId, password);

            if (verified) {
                logger.info("비밀번호 확인 성공: 사용자={}", userId);
                response.put("success", true);
                response.put("message", "비밀번호가 확인되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("비밀번호 확인 실패: 사용자={}", userId);
                response.put("success", false);
                response.put("message", "비밀번호가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalStateException e) {
            logger.warn("소셜 로그인 사용자는 비밀번호 확인 불가: 사용자={}", token);
            response.put("success", false);
            response.put("message", "소셜 로그인 사용자는 비밀번호 확인이 불가능합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("비밀번호 확인 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 비밀번호 변경 (로그인한 사용자)
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> passwordData,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String authToken = token.replace("Bearer ", "");
            String userId = jwtUtil.extractUsername(authToken);
            String newPassword = passwordData.get("newPassword");

            if (newPassword == null || newPassword.length() < 6) {
                logger.warn("비밀번호 변경 실패: 사용자={}, 비밀번호 유효성 검사 실패", userId);
                response.put("success", false);
                response.put("message", "비밀번호는 최소 6자리 이상이어야 합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (userService.changePassword(userId, newPassword)) {
                logger.info("비밀번호 변경 성공: 사용자={}", userId);
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("비밀번호 변경 실패: 사용자={}", userId);
                response.put("success", false);
                response.put("message", "비밀번호 변경에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalStateException e) {
            logger.warn("소셜 로그인 사용자는 비밀번호 변경 불가: 사용자={}", token);
            response.put("success", false);
            response.put("message", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("비밀번호 변경 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String authToken = token.replace("Bearer ", "");
            String userId = jwtUtil.extractUsername(authToken);

            Optional<User> optionalUser = userService.findUserById(userId);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                response.put("name", user.getName());
                response.put("email", user.getEmail());
                response.put("phone", user.getPhone());
                response.put("provider", user.getProvider());
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, String> body) {
        String token = authHeader.replace("Bearer", "").trim();
        String userId = jwtUtil.extractUsername(token);
        String reason = body.get("reason");

        logger.info("[회원탈퇴 요청] 사용자={}, 사유={}", userId, reason);

        try {
            userService.deleteUserAndRelatedData(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원 탈퇴가 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("회원 탈퇴 중 오류 발생", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }




    // 6자리 인증번호 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 100000-999999 범위의 난수 생성
        return String.valueOf(code);
    }
}