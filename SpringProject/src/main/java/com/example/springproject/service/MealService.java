package com.example.springproject.service;

import com.example.springproject.model.Meal;
import com.example.springproject.model.User;

import java.time.LocalDate;
import java.util.List;

public interface MealService {
    void save(Meal meal, User user);
    List<Meal> getMealsByDate(User user, LocalDate date);
    List<Meal> getMealsByPeriod(User user, LocalDate start, LocalDate end);
    int getTotalCalories(User user, LocalDate date);
    int getTotalCalories(User user, LocalDate start, LocalDate end);
    void deleteById(Long id);

    void deleteByUser(User user);

    int getTodayCaloriesByUserId(String userId, LocalDate date) throws Exception;


} 