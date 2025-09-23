package com.example.springproject.service;

import com.example.springproject.model.User;
import com.example.springproject.model.Weight;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface WeightService {

    // User 객체로 변경
    List<Weight> getWeightRecordsByUser(User user) throws SQLException;

    void save(Weight weight) throws SQLException;

    Weight getWeightRecordByUserAndDate(String userId, LocalDate date) throws SQLException;

    Weight getWeightRecordById(Long id) throws SQLException;

    void update(Weight weight) throws SQLException;

    void deleteById(Long id) throws SQLException;

    Weight findLatestByUserId(String userId) throws SQLException;

}
