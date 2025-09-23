package com.example.springproject.model;

import com.example.springproject.model.User;

public class CustomReport {

    private User user;
    private double latestWeight;
    private double bmr;
    private double totalCalories;
    private double recommendedCalories;
    private double calorieDifference;
    private String exerciseRecommendation;

    // getter, setter

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatestWeight() {
        return latestWeight;
    }

    public void setLatestWeight(double latestWeight) {
        this.latestWeight = latestWeight;
    }

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getRecommendedCalories() {
        return recommendedCalories;
    }

    public void setRecommendedCalories(double recommendedCalories) {
        this.recommendedCalories = recommendedCalories;
    }

    public double getCalorieDifference() {
        return calorieDifference;
    }

    public void setCalorieDifference(double calorieDifference) {
        this.calorieDifference = calorieDifference;
    }

    public String getExerciseRecommendation() {
        return exerciseRecommendation;
    }

    public void setExerciseRecommendation(String exerciseRecommendation) {
        this.exerciseRecommendation = exerciseRecommendation;
    }
}
