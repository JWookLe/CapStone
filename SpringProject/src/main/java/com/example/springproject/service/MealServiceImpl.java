package com.example.springproject.service;

import com.example.springproject.model.Meal;
import com.example.springproject.model.User;
import com.example.springproject.repository.MealDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

@Service
public class MealServiceImpl implements MealService {
    @Autowired
    private MealDao mealDao;

    @Override
    public void save(Meal meal, User user) {
        meal.setUserId(user.getId());
        try {
            mealDao.save(meal);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Meal> getMealsByDate(User user, LocalDate date) {
        try {
            return mealDao.findByUserAndDate(user.getId(), date);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Meal> getMealsByPeriod(User user, LocalDate start, LocalDate end) {
        try {
            return mealDao.findByUserAndDateBetween(user.getId(), start, end);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getTotalCalories(User user, LocalDate date) {
        try {
            return mealDao.findByUserAndDate(user.getId(), date).stream().mapToInt(Meal::getCalories).sum();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getTotalCalories(User user, LocalDate start, LocalDate end) {
        try {
            return mealDao.findByUserAndDateBetween(user.getId(), start, end).stream().mapToInt(Meal::getCalories).sum();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            mealDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByUser(User user) {
        try {
            mealDao.deleteAllByUser(user.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getTodayCaloriesByUserId(String userId, LocalDate date) throws Exception {
        return mealDao.getTodayCaloriesByUserId(userId, date);
    }
} 