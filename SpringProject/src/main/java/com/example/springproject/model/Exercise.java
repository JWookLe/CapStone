package com.example.springproject.model;

import java.time.LocalDate;

public class Exercise {
    private Long id;
    private String exerciseName;
    private String detailExerciseName;
    private int burnedCalories;
    private LocalDate date;
    private String userId; // user 객체 대신 userId만 보관

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
    public String getDetailExerciseName() { return detailExerciseName; }
    public void setDetailExerciseName(String detailExerciseName) { this.detailExerciseName = detailExerciseName; }
    public int getBurnedCalories() { return burnedCalories; }
    public void setBurnedCalories(int burnedCalories) { this.burnedCalories = burnedCalories; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
} 