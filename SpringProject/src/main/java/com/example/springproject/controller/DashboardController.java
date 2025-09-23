// DashboardController.java
package com.example.springproject.controller;

import com.example.springproject.model.User;
import com.example.springproject.model.Goal;
import com.example.springproject.service.MealService;
import com.example.springproject.service.ExerciseService;
import com.example.springproject.service.GoalService;
import com.example.springproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Controller
public class DashboardController {
    @Autowired
    private MealService mealService;
    @Autowired
    private ExerciseService exerciseService;
    @Autowired
    private GoalService goalService;
    @Autowired
    private UserService userService;

    @GetMapping({"/dashboard", "/"})
    public String dashboard(@RequestParam(value = "date", required = false) String date, Model model, Principal principal) {
        User user = userService.findById(principal.getName());
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        String todayStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        int totalCalories = mealService.getTotalCalories(user, targetDate);
        int totalBurned = exerciseService.getTotalBurned(user, targetDate);

        Goal currentGoal = goalService.getCurrentGoalByDate(user, targetDate);
        int targetCalories = 0;
        boolean isOverTarget = false;

        if (currentGoal != null && currentGoal.getTargetCalories() != null) {
            targetCalories = currentGoal.getTargetCalories();
            isOverTarget = (totalCalories - totalBurned) > targetCalories;
        }

        model.addAttribute("todayMeals", mealService.getMealsByDate(user, targetDate));
        model.addAttribute("todayExercises", exerciseService.getExercisesByDate(user, targetDate));
        model.addAttribute("todayTotalCalories", totalCalories);
        model.addAttribute("todayTotalBurned", totalBurned);
        model.addAttribute("currentGoal", currentGoal);
        model.addAttribute("today", targetDate);
        model.addAttribute("todayStr", todayStr);
        model.addAttribute("isOverTarget", isOverTarget);

        return "dashboard";
    }

    @GetMapping("/dashboard/weekly-report")
    public String weeklyReport(Model model, Principal principal) {
        User user = userService.findById(principal.getName());
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        List<com.example.springproject.model.Meal> meals = mealService.getMealsByPeriod(user, startOfWeek, endOfWeek);
        List<com.example.springproject.model.Exercise> exercises = exerciseService.getExercisesByPeriod(user, startOfWeek, endOfWeek);
        int totalCalories = meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
        int totalBurned = exercises.stream().mapToInt(e -> e.getBurnedCalories()).sum();
        double avgCalories = meals.isEmpty() ? 0 : totalCalories / (double) meals.size();
        double avgBurned = exercises.isEmpty() ? 0 : totalBurned / (double) exercises.size();
        model.addAttribute("meals", meals);
        model.addAttribute("exercises", exercises);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalBurned", totalBurned);
        model.addAttribute("avgCalories", avgCalories);
        model.addAttribute("avgBurned", avgBurned);
        model.addAttribute("startOfWeek", startOfWeek);
        model.addAttribute("endOfWeek", endOfWeek);
        model.addAttribute("todayStr", todayStr);
        return "weeklyReport";
    }

    @GetMapping("/monthlyReport")
    public String monthlyReport(Model model, Principal principal) {
        User user = userService.findById(principal.getName());
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        List<com.example.springproject.model.Meal> meals = mealService.getMealsByPeriod(user, startOfMonth, endOfMonth);
        List<com.example.springproject.model.Exercise> exercises = exerciseService.getExercisesByPeriod(user, startOfMonth, endOfMonth);
        int totalCalories = meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
        int totalBurned = exercises.stream().mapToInt(e -> e.getBurnedCalories()).sum();
        double avgCalories = meals.isEmpty() ? 0 : totalCalories / (double) meals.size();
        double avgBurned = exercises.isEmpty() ? 0 : totalBurned / (double) exercises.size();
        model.addAttribute("meals", meals);
        model.addAttribute("exercises", exercises);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalBurned", totalBurned);
        model.addAttribute("avgCalories", avgCalories);
        model.addAttribute("avgBurned", avgBurned);
        model.addAttribute("startOfMonth", startOfMonth);
        model.addAttribute("endOfMonth", endOfMonth);
        model.addAttribute("todayStr", todayStr);
        return "monthlyReport";
    }
}
