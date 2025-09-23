package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.dto.UserDto;
import com.example.EmotionSyncServer.jwt.JwtUtil;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(KakaoAuthService.class);

    @Autowired
    public KakaoAuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> authenticateKakaoUser(String kakaoToken) {
        Map<String, Object> response = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        String userInfoEndpoint = "https://kapi.kakao.com/v2/user/me";

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + kakaoToken);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            var responseEntity = restTemplate.exchange(userInfoEndpoint, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = responseEntity.getBody();

            if (body == null || !body.containsKey("kakao_account")) {
                throw new RuntimeException("카카오 계정 정보 없음");
            }

            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");
            String name = profile != null ? (String) profile.get("nickname") : null;
            String kakaoId = String.valueOf(body.get("id"));

            if (email == null || email.isEmpty()) {
                email = "kakao_" + kakaoId + "@kakao.local";
            }
            if (name == null || name.isEmpty()) {
                name = "카카오유저";
            }

            final String userEmail = email;
            final String userName = name;

            Optional<User> existingUser = userRepository.findByEmail(userEmail);
            User user = existingUser.orElseGet(() -> {
                User newUser = new User();
                //websocket 접속용
                //newUser.setProviderKey(kakaoId);

                newUser.setId(userEmail);
                newUser.setEmail(userEmail);
                newUser.setName(userName);
                newUser.setPassword(null);
                newUser.setPhone(null);
                newUser.setProvider("KAKAO");
                return userRepository.save(newUser);
            });

            String token = jwtUtil.generateToken(user.getEmail());
            UserDto userDto = new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPhone(),
                    user.getProvider(),
                    user.getInviteCode()
            );

            response.put("success", true);
            response.put("token", token);
            response.put("user", userDto);
            return response;

        } catch (Exception e) {
            logger.error("카카오 사용자 인증 실패", e);
            throw new RuntimeException("카카오 사용자 인증 실패", e);
        }
    }
}
