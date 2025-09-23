package com.example.EmotionSyncServer.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY_STRING = "my-super-secure-jwt-secret-key-2025-very-long-and-random";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token.trim())
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token.trim());
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println("잘못된 형식의 토큰입니다: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.out.println("토큰 서명이 유효하지 않습니다: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.out.println("지원하지 않는 토큰입니다: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            System.out.println("토큰이 비어있습니다: " + e.getMessage());
            throw e;
        }
    }
}