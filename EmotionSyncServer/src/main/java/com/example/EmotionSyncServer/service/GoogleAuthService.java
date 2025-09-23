package com.example.EmotionSyncServer.service;

import com.example.EmotionSyncServer.dto.UserDto;
import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.UserRepository;
import com.example.EmotionSyncServer.jwt.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

@Service
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String webClientId;

    private final String androidClientId = "807266536718-b0o2vcqtollrgf4t55bofqbfur9l2iqq.apps.googleusercontent.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> authenticateGoogleUser(String idTokenString) {
        try {
            System.out.println("🛑 받은 idToken: " + idTokenString);

            String[] parts = idTokenString.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("ID Token 형식 오류");
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            System.out.println("🛑 idToken Payload: " + payloadJson);

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Arrays.asList(androidClientId, webClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String sub = payload.getSubject(); // 🔥 추가: 구글 고유 ID(sub)

                System.out.println("🛑 검증 성공! email=" + email + ", name=" + name + ", sub=" + sub);

                Optional<User> existingUser = userRepository.findByEmail(email);
                User user = existingUser.orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(email);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(null);
                    newUser.setPhone(null);
                    newUser.setProvider("GOOGLE");
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

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("token", token);
                response.put("user", userDto);
                return response;
            }

            throw new RuntimeException("Invalid ID token - 검증 실패");

        } catch (Exception e) {
            System.out.println("🛑 예외 발생: " + e.getMessage());
            throw new RuntimeException("Failed to authenticate with Google", e);
        }
    }
}
