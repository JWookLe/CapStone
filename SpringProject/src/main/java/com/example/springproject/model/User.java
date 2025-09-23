package com.example.springproject.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class User {
    private String id;
    private String username;
    private String password;
    private String passwordConfirm;
    private BigDecimal height;  // 키 (cm 단위)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;  // 생년월일
    private String role;
    private String gender;   // 성별 ("male" 또는 "female")
    private boolean isBanned;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    // height getter/setter
    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    // gender getter/setter
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBanned() {
        return isBanned;
    }
    public void setBanned(boolean banned) {
        this.isBanned = banned;
    }
}
