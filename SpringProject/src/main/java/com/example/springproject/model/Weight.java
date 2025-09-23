package com.example.springproject.model;

import java.time.LocalDate;

public class Weight {
    private Long id;
    private String userId; // User 객체 대신 User ID 문자열이나 숫자 사용
    private LocalDate date;
    private Double weight;

    public Weight() {}

    public Weight(Long id, String userId, LocalDate date, Double weight) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    // getter & setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
