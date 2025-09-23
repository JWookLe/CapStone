package com.example.springproject.repository;

import com.example.springproject.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExerciseDao {
    @Autowired
    private DataSource dataSource;

    public List<Exercise> findByUserAndDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM exercise WHERE user_id = ? AND date = ?";
        List<Exercise> exercises = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = mapRow(rs);
                    exercises.add(exercise);
                }
            }
        }
        return exercises;
    }

    public List<Exercise> findByUserAndDateBetween(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM exercise WHERE user_id = ? AND date BETWEEN ? AND ?";
        List<Exercise> exercises = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = mapRow(rs);
                    exercises.add(exercise);
                }
            }
        }
        return exercises;
    }

    public void save(Exercise exercise) throws SQLException {
        String sql = "INSERT INTO exercise (exerciseName, detailExerciseName, burnedCalories, date, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exercise.getExerciseName());
            ps.setString(2, exercise.getDetailExerciseName());
            ps.setInt(3, exercise.getBurnedCalories());
            ps.setDate(4, Date.valueOf(exercise.getDate()));
            ps.setString(5, exercise.getUserId());
            ps.executeUpdate();
        }
    }

    public void update(Exercise exercise) throws SQLException {
        String sql = "UPDATE exercise SET exerciseName=?, detailExerciseName=?, burnedCalories=?, date=? WHERE id=? AND user_id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exercise.getExerciseName());
            ps.setString(2, exercise.getDetailExerciseName());
            ps.setInt(3, exercise.getBurnedCalories());
            ps.setDate(4, Date.valueOf(exercise.getDate()));
            ps.setLong(5, exercise.getId());
            ps.setString(6, exercise.getUserId());
            ps.executeUpdate();
        }
    }

    public void deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM exercise WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Exercise mapRow(ResultSet rs) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setId(rs.getLong("id"));
        exercise.setExerciseName(rs.getString("exerciseName"));
        exercise.setDetailExerciseName(rs.getString("detailExerciseName"));
        exercise.setBurnedCalories(rs.getInt("burnedCalories"));
        java.sql.Date sqlDate = rs.getDate("date");
        if (sqlDate != null) {
            exercise.setDate(sqlDate.toLocalDate());
        } else {
            exercise.setDate(null);
        }
        exercise.setUserId(rs.getString("user_id"));
        return exercise;
    }
} 