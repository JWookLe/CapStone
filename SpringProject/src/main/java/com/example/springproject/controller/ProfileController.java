package com.example.springproject.controller;

import com.example.springproject.model.User;
import com.example.springproject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @GetMapping("")
    public String profile(Model model, Authentication authentication) {
        User user = userService.findById(authentication.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 Model model,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User user = userService.findById(authentication.getName());
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("error", "모든 정보를 입력하세요.");
            model.addAttribute("user", user);
            return "profile";
        }
        if (newPassword.length() < 8) {
            model.addAttribute("error", "새 비밀번호는 8자리 이상이어야 합니다.");
            model.addAttribute("user", user);
            return "profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "새 비밀번호와 확인이 일치하지 않습니다.");
            model.addAttribute("user", user);
            return "profile";
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            model.addAttribute("user", user);
            return "profile";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);
        session.setAttribute("passwordChanged", true);
        return "redirect:" + request.getContextPath() + "/login";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam(required = false) String reason, Authentication authentication, HttpSession session, HttpServletRequest request, Model model) {
        User user = userService.findById(authentication.getName());
        if (reason == null || reason.trim().isEmpty()) {
            model.addAttribute("error", "탈퇴 사유를 선택해 주세요.");
            model.addAttribute("user", user);
            return "profile";
        }
        logger.info("탈퇴 사유: {} (user: {})", reason, user.getUsername());
        userService.delete(user);
        session.setAttribute("deleted", true);
        return "redirect:" + request.getContextPath() + "/login";
    }
} 