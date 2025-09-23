package com.example.springproject.service;

import com.example.springproject.model.User;
import com.example.springproject.model.Weight;
import com.example.springproject.repository.WeightDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class WeightServiceImpl implements WeightService {

    @Autowired
    private WeightDao weightDao;

    @Override
    public List<Weight> getWeightRecordsByUser(User user) throws SQLException {
        // User 객체에서 userId 가져오기
        return weightDao.findByUserId(user.getId());
    }

    @Override
    public void save(Weight weight) throws SQLException {
        weightDao.save(weight);
    }

    @Override
    public Weight getWeightRecordByUserAndDate(String userId, LocalDate date) throws SQLException {
        return weightDao.findByUserIdAndDate(userId, date);
    }

    @Override
    public Weight getWeightRecordById(Long id) throws SQLException {
        return weightDao.findById(id);
    }

    @Override
    public void update(Weight weight) throws SQLException {
        weightDao.update(weight);
    }

    @Override
    public void deleteById(Long id) throws SQLException {
        weightDao.deleteById(id);
    }
    @Override
    public Weight findLatestByUserId(String userId) throws SQLException {
        return weightDao.findLatestByUserId(userId);
    }
}
