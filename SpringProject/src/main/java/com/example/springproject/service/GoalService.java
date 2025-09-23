package com.example.springproject.service;

import com.example.springproject.model.Goal;
import com.example.springproject.model.User;

import java.util.List;
import java.time.LocalDate;

public interface GoalService {
    void save(Goal goal, User user);
    List<Goal> getGoals(User user);
    Goal getCurrentGoal(User user);
    void delete(Goal goal);
    List<Goal> getGoalsByDate(User user, LocalDate date);
    Goal getCurrentGoalByDate(User user, LocalDate date);
    int getTargetCaloriesByUserAndDate(User user, LocalDate date);
} 