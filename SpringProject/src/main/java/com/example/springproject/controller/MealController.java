package com.example.springproject.controller;

import com.example.springproject.model.Meal;
import com.example.springproject.model.User;
import com.example.springproject.service.MealService;
import com.example.springproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.beans.PropertyEditorSupport;

@Controller
public class MealController {
    @Autowired
    private MealService mealService;
    @Autowired
    private UserService userService;

    @GetMapping("/meals")
    public String meals(Model model, Principal principal, @RequestParam(value = "searchDate", required = false) String dateStr) {
        User user = userService.findById(principal.getName());
        LocalDate date;
        if (dateStr != null && !dateStr.isEmpty()) {
            date = LocalDate.parse(dateStr);
        } else {
            date = LocalDate.now();
        }
        List<Meal> meals = mealService.getMealsByDate(user, date);
        int totalCalories = mealService.getTotalCalories(user, date);
        model.addAttribute("mealForm", new Meal());
        model.addAttribute("meals", meals);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("today", date);
        return "meals";
    }

    @PostMapping("/meals")
    public String addMeal(@ModelAttribute("mealForm") Meal meal, Principal principal, Model model, org.springframework.validation.BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            User user = userService.findById(principal.getName());
            LocalDate today = LocalDate.now();
            List<Meal> meals = mealService.getMealsByDate(user, today);
            int totalCalories = mealService.getTotalCalories(user, today);
            model.addAttribute("mealForm", new Meal());
            model.addAttribute("meals", meals);
            model.addAttribute("totalCalories", totalCalories);
            model.addAttribute("today", today);
            model.addAttribute("error", "입력값이 올바르지 않습니다. (날짜 형식 등)");
            return "meals";
        }
        if (meal.getFoodName() == null || meal.getFoodName().trim().isEmpty() ||
            meal.getCalories() == null ||
            meal.getDate() == null ||
            meal.getMealType() == null || meal.getMealType().trim().isEmpty()) {
            User user = userService.findById(principal.getName());
            LocalDate today = LocalDate.now();
            List<Meal> meals = mealService.getMealsByDate(user, today);
            int totalCalories = mealService.getTotalCalories(user, today);
            model.addAttribute("mealForm", new Meal());
            model.addAttribute("meals", meals);
            model.addAttribute("totalCalories", totalCalories);
            model.addAttribute("today", today);
            model.addAttribute("error", "모든 정보를 입력하세요.");
            return "meals";
        }
        User user = userService.findById(principal.getName());
        mealService.save(meal, user);
        return "redirect:/meals";
    }

    @GetMapping("/meals/delete/{id}")
    public String deleteMeal(@PathVariable("id") Long id, Principal principal) {
        mealService.deleteById(id);
        return "redirect:/meals";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
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
} 