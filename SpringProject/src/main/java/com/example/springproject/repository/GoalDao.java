package com.example.springproject.repository;

import com.example.springproject.model.Goal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GoalDao {
    @Autowired
    private DataSource dataSource;

    public List<Goal> findByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM goal WHERE user_id = ? ORDER BY id DESC";
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goal goal = mapRow(rs);
                    goals.add(goal);
                }
            }
        }
        return goals;
    }

    public void save(Goal goal) throws SQLException {
        String sql = "INSERT INTO goal (targetCalories, user_id, date) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goal.getTargetCalories());
            ps.setString(2, goal.getUserId());
            ps.setDate(3, java.sql.Date.valueOf(goal.getDate()));
            ps.executeUpdate();
        }
    }

    public void update(Goal goal) throws SQLException {
        String sql = "UPDATE goal SET targetCalories=?, date=? WHERE id=? AND user_id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goal.getTargetCalories());
            ps.setDate(2, java.sql.Date.valueOf(goal.getDate()));
            ps.setLong(3, goal.getId());
            ps.setString(4, goal.getUserId());
            ps.executeUpdate();
        }
    }

    public void deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM goal WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<Goal> findByUserAndDate(String userId, java.time.LocalDate date) throws SQLException {
        String sql = "SELECT * FROM goal WHERE user_id = ? AND date = ? ORDER BY id DESC";
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Goal goal = mapRow(rs);
                    goals.add(goal);
                }
            }
        }
        return goals;
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getLong("id"));
        goal.setTargetCalories(rs.getInt("targetCalories"));
        goal.setUserId(rs.getString("user_id"));
        java.sql.Date sqlDate = rs.getDate("date");
        if (sqlDate != null) {
            goal.setDate(sqlDate.toLocalDate());
        } else {
            goal.setDate(null);
        }
        return goal;
    }
} 