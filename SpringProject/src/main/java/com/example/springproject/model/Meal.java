package com.example.springproject.model;

import java.time.LocalDate;

public class Meal {
    private Long id;
    private String foodName;
    private Integer calories;
    private LocalDate date;
    private String mealType;  // 아침/점심/저녁
    private String userId;    // user 객체 대신 userId만 보관

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMealType() {return mealType;}
    public void setMealType(String mealType) {this.mealType = mealType;}
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
} 