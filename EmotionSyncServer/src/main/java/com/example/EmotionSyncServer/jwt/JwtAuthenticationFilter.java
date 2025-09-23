package com.example.EmotionSyncServer.jwt;

import com.example.EmotionSyncServer.model.User;
import com.example.EmotionSyncServer.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔓 인증 제외 경로
        if (path.startsWith("/api/users/login") || path.startsWith("/api/users/register") || path.startsWith("/api/shares/")) {
            System.out.println("🔓 JWT 필터: 인증 제외 경로 통과 → " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
            String token = authHeader.substring(7);
                String userId = jwtUtil.extractUsername(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

                    if (jwtUtil.validateToken(token)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ JWT 필터: 사용자 인증 성공 → " + userId);
                    }
                }
            } catch (ExpiredJwtException e) {
                System.out.println("❌ JWT 필터: 토큰 만료 → " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("토큰이 만료되었습니다.");
                return;
            } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
                System.out.println("❌ JWT 필터: 토큰 검증 실패 → " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("유효하지 않은 토큰입니다.");
                return;
            } catch (UsernameNotFoundException e) {
                System.out.println("❌ JWT 필터: 사용자 없음 → " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("사용자를 찾을 수 없습니다.");
                return;
            } catch (Exception e) {
                System.out.println("❌ JWT 필터: 예외 발생 → " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("서버 오류가 발생했습니다.");
                return;
            }
        }

        // 🔁 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
