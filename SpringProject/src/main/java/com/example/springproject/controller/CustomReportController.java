package com.example.springproject.controller;

import com.example.springproject.model.Goal;
import com.example.springproject.model.User;
import com.example.springproject.model.Weight;
import com.example.springproject.service.GoalService;
import com.example.springproject.service.UserService;
import com.example.springproject.service.WeightService;
import com.example.springproject.service.MealService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;

@Controller
public class CustomReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private WeightService weightService;

    @Autowired
    private MealService mealService;

    @Autowired
    private GoalService goalService;

    @GetMapping("/customReport")
    public String getCustomReport(Principal principal, Model model) throws Exception {
        if (principal == null) {
            System.out.println("🔴 principal is null (로그인 안됨)");
            return "customReport"; // 로그인 안 됐을 때도 JSP 렌더링, 변수는 없겠지만
        }
        String userId = principal.getName();
        System.out.println("✅ principal.getName(): " + userId);

        User user = userService.findById(userId);
        if (user == null) {
            System.out.println("🔴 userService.findById(userId) 결과 없음: " + userId);
            return "customReport";
        }

        Weight latestWeightObj = weightService.findLatestByUserId(userId);
        if (latestWeightObj == null) {
            System.out.println("🔴 weightService.findLatestByUserId(userId) 결과 없음: " + userId);
            model.addAttribute("msg", "몸무게를 먼저 입력해주세요.");
            model.addAttribute("weightForm", new Weight());  // ✅ 필수
            return "weightChange";
        }

        System.out.println("✅ 사용자 정보: username=" + user.getUsername() + ", gender=" + user.getGender() +
                ", height=" + user.getHeight() + ", birthDate=" + user.getBirthDate());

        double weight = latestWeightObj.getWeight().doubleValue();
        double height = user.getHeight().doubleValue();
        int age = calculateAge(user.getBirthDate(), LocalDate.now());

        System.out.println("✅ 최신 몸무게: " + weight + "kg");
        System.out.println("✅ 계산된 나이: " + age + "세");

        double bmr = calculateBmr(user.getGender(), age, height, weight);
        System.out.println("✅ BMR 계산 결과: " + bmr);

        LocalDate today = LocalDate.now();
        int totalCalories = mealService.getTodayCaloriesByUserId(userId, today);
        System.out.println("✅ 오늘 섭취 칼로리: " + totalCalories);

        int targetCalories = goalService.getTargetCaloriesByUserAndDate(user, today);
        System.out.println("✅ 목표 칼로리: " + targetCalories);

        double calorieDifference = bmr - totalCalories;
        System.out.println("✅ 칼로리 차이: " + calorieDifference);

        String exerciseRecommendation = getExerciseRecommendation(calorieDifference);
        System.out.println("✅ 추천 운동: " + exerciseRecommendation);

        // JSP에 전달할 변수 등록
        model.addAttribute("username", user.getUsername());
        model.addAttribute("gender", user.getGender());
        model.addAttribute("age", age);
        model.addAttribute("height", height);
        model.addAttribute("weight", weight);
        model.addAttribute("bmr", String.format("%.1f", bmr));  // 소수점 1자리로 포맷팅
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("targetCalories", targetCalories);
        model.addAttribute("calorieDifference", calorieDifference);
        model.addAttribute("exerciseRecommendation", exerciseRecommendation);

        return "customReport";  // /WEB-INF/views/customReport.jsp
    }

    private int calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if (birthDate == null || currentDate == null) {
            System.out.println("⚠️ 생년월일 또는 현재 날짜 null");
            return 0;
        }
        return Period.between(birthDate, currentDate).getYears();
    }

    private double calculateBmr(String gender, int age, double height, double weight) {
        double bmr;
        if ("male".equalsIgnoreCase(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }
        return bmr;
    }

    private String getExerciseRecommendation(double calorieDifference) {
        if (calorieDifference > 500) {
            return "칼로리를 더 섭취를 추천합니다.";
        } else if (calorieDifference > 200) {
            return "적당한 운동이 필요합니다. 걷기나 가벼운 조깅을 추천합니다.";
        } else if (calorieDifference > -200) {
            return "현재 상태를 유지하기 위해 꾸준한 운동이 필요합니다.";
        } else {
            return "칼로리 섭취가 많습니다. 식단 조절과 함께 운동량을 늘리세요.";
        }
    }
}
