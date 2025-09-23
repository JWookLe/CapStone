package com.example.springproject.repository;

public class UserRankDto {
    private String username;
    private int value; // burnedCalories 또는 weightLoss 등

    public UserRankDto(String username, int value) {
        this.username = username;
        this.value = value;
    }

    // getter, setter
    public String getUsername() {
        return username;
    }

    public int getValue() {
        return value;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
