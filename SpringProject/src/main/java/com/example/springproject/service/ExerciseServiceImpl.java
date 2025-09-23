package com.example.springproject.service;

import com.example.springproject.model.Exercise;
import com.example.springproject.model.User;
import com.example.springproject.repository.ExerciseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    @Autowired
    private ExerciseDao exerciseDao;

    @Override
    public void save(Exercise exercise, User user) {
        exercise.setUserId(user.getId());
        try {
            exerciseDao.save(exercise);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Exercise> getExercisesByDate(User user, LocalDate date) {
        try {
            return exerciseDao.findByUserAndDate(user.getId(), date);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Exercise> getExercisesByPeriod(User user, LocalDate start, LocalDate end) {
        try {
            return exerciseDao.findByUserAndDateBetween(user.getId(), start, end);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getTotalBurned(User user, LocalDate date) {
        try {
            return exerciseDao.findByUserAndDate(user.getId(), date).stream().mapToInt(Exercise::getBurnedCalories).sum();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getTotalBurned(User user, LocalDate start, LocalDate end) {
        try {
            return exerciseDao.findByUserAndDateBetween(user.getId(), start, end).stream().mapToInt(Exercise::getBurnedCalories).sum();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            exerciseDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
} 