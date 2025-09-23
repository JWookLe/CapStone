package com.example.springproject.service;

import com.example.springproject.model.Exercise;
import com.example.springproject.model.User;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseService {
    void save(Exercise exercise, User user);
    List<Exercise> getExercisesByDate(User user, LocalDate date);
    List<Exercise> getExercisesByPeriod(User user, LocalDate start, LocalDate end);
    int getTotalBurned(User user, LocalDate date);
    int getTotalBurned(User user, LocalDate start, LocalDate end);
    void deleteById(Long id);
} 