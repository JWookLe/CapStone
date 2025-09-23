package com.example.springproject.controller;

import com.example.springproject.model.Goal;
import com.example.springproject.model.User;
import com.example.springproject.service.GoalService;
import com.example.springproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.beans.PropertyEditorSupport;

@Controller
public class GoalController {
    @Autowired
    private GoalService goalService;
    @Autowired
    private UserService userService;

    @GetMapping("/goal")
    public String goal(Model model, Principal principal, @RequestParam(value = "searchDate", required = false) String dateStr) {
        User user = userService.findById(principal.getName());
        LocalDate date;
        if (dateStr != null && !dateStr.isEmpty()) {
            date = LocalDate.parse(dateStr);
        } else {
            date = LocalDate.now();
        }
        List<Goal> goals = goalService.getGoalsByDate(user, date);
        Goal currentGoal = goalService.getCurrentGoalByDate(user, date);
        model.addAttribute("goalForm", new Goal());
        model.addAttribute("goals", goals);
        model.addAttribute("currentGoal", currentGoal);
        model.addAttribute("today", date);
        return "goal";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(java.time.LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    setValue(LocalDate.parse(text));
                }
            }
        });
    }

    @PostMapping("/goal")
    public String setGoal(@ModelAttribute("goalForm") Goal goal, Principal principal, Model model, org.springframework.validation.BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            User user = userService.findById(principal.getName());
            List<Goal> goals = goalService.getGoals(user);
            Goal currentGoal = goalService.getCurrentGoal(user);
            model.addAttribute("goalForm", new Goal());
            model.addAttribute("goals", goals);
            model.addAttribute("currentGoal", currentGoal);
            model.addAttribute("error", "입력값이 올바르지 않습니다. (날짜 형식 등)");
            return "goal";
        }
        if (goal.getTargetCalories() == null) {
            User user = userService.findById(principal.getName());
            List<Goal> goals = goalService.getGoals(user);
            Goal currentGoal = goalService.getCurrentGoal(user);
            model.addAttribute("goalForm", new Goal());
            model.addAttribute("goals", goals);
            model.addAttribute("currentGoal", currentGoal);
            model.addAttribute("error", "목표 칼로리를 입력하세요.");
            return "goal";
        }
        if (goal.getDate() == null) {
            goal.setDate(LocalDate.now());
        }
        User user = userService.findById(principal.getName());
        // 같은 날짜의 목표가 이미 있으면 삭제(덮어쓰기)
        List<Goal> goals = goalService.getGoalsByDate(user, goal.getDate());
        if (!goals.isEmpty()) {
            for (Goal g : goals) {
                goalService.delete(g);
            }
        }
        goalService.save(goal, user);

        return "redirect:/goal?searchDate=" + goal.getDate();
    }

    @PostMapping("/goal/delete")
    public String deleteGoal(Principal principal) {
        User user = userService.findById(principal.getName());
        Goal currentGoal = goalService.getCurrentGoal(user);
        if (currentGoal != null) {
            goalService.delete(currentGoal);
        }
        return "redirect:/goal";
    }
} 