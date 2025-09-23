package com.example.springproject.repository;

import com.example.springproject.model.Meal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MealDao {
    @Autowired
    private DataSource dataSource;

    public List<Meal> findByUserAndDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM meal WHERE user_id = ? AND date = ?";
        List<Meal> meals = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Meal meal = mapRow(rs);
                    meals.add(meal);
                }
            }
        }
        return meals;
    }

    public List<Meal> findByUserAndDateBetween(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM meal WHERE user_id = ? AND date BETWEEN ? AND ?";
        List<Meal> meals = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Meal meal = mapRow(rs);
                    meals.add(meal);
                }
            }
        }
        return meals;
    }

    public void deleteAllByUser(String userId) throws SQLException {
        String sql = "DELETE FROM meal WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    public void save(Meal meal) throws SQLException {
        String sql = "INSERT INTO meal (foodName, calories, date, mealType, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meal.getFoodName());
            ps.setInt(2, meal.getCalories());
            ps.setDate(3, Date.valueOf(meal.getDate()));
            ps.setString(4, meal.getMealType());
            ps.setString(5, meal.getUserId());
            ps.executeUpdate();
        }
    }

    public void update(Meal meal) throws SQLException {
        String sql = "UPDATE meal SET foodName=?, calories=?, date=?, mealType=? WHERE id=? AND user_id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meal.getFoodName());
            ps.setInt(2, meal.getCalories());
            ps.setDate(3, Date.valueOf(meal.getDate()));
            ps.setString(4, meal.getMealType());
            ps.setLong(5, meal.getId());
            ps.setString(6, meal.getUserId());
            ps.executeUpdate();
        }
    }

    public void deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM meal WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Meal mapRow(ResultSet rs) throws SQLException {
        Meal meal = new Meal();
        meal.setId(rs.getLong("id"));
        meal.setFoodName(rs.getString("foodName"));
        meal.setCalories(rs.getInt("calories"));
        java.sql.Date sqlDate = rs.getDate("date");
        if (sqlDate != null) {
            meal.setDate(sqlDate.toLocalDate());
        } else {
            meal.setDate(null);
        }
        meal.setMealType(rs.getString("mealType"));
        meal.setUserId(rs.getString("user_id"));
        return meal;
    }
    public int getTodayCaloriesByUserId(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT SUM(calories) FROM meal WHERE user_id = ? AND date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);  // 합계 리턴, null은 0으로 처리됨
                }
            }
        }
        return 0;
    }

} 