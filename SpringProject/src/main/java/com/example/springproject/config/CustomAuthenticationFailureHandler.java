package com.example.springproject.config;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.*;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        System.out.println("[DEBUG] 로그인 실패한 사용자: " + username);

        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        if (exception instanceof DisabledException) {
            System.out.println("[DEBUG] 해당 사용자는 is_banned = true 상태입니다.");
            errorMessage = "이 계정은 이용이 정지되었습니다.";
        }

        // 세션에 메시지를 저장하고 리다이렉트
        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect("/login?error=true");
    }
}
