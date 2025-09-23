package com.example.springproject.controller;

import com.example.springproject.repository.UserRankDto;
import com.example.springproject.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChallengeController {

    private final ChallengeService challengeService;

    @Autowired
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/challenge")
    public String showRankings(Model model) {
        List<UserRankDto> exerciseRanks = challengeService.getTodayExcerciseKing();
        List<UserRankDto> mealRanks = challengeService.getTodayMealKing();
        List<UserRankDto> weightLossRanks = challengeService.getMonthWeightLossKing();

        if (exerciseRanks == null) {
            model.addAttribute("exerciseMessage", "오늘의 운동왕 기록이 없습니다.");
        } else {
            model.addAttribute("exerciseRanks", exerciseRanks);
        }

        if (mealRanks == null) {
            model.addAttribute("mealMessage", "오늘의 식사왕 기록이 없습니다.");
        } else {
            model.addAttribute("mealRanks", mealRanks);
        }

        if (weightLossRanks == null) {
            model.addAttribute("weightLossMessage", "이번 달의 감량왕 기록이 없습니다.");
        } else {
            model.addAttribute("weightLossRanks", weightLossRanks);
        }

        return "challenge"; // JSP 또는 Thymeleaf 뷰 이름
    }
}
