package com.example.springproject.service;

import com.example.springproject.repository.ChallengeDao;
import com.example.springproject.repository.UserRankDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeDao challengeDao;

    @Autowired
    public ChallengeServiceImpl(ChallengeDao challengeDao) {
        this.challengeDao = challengeDao;
    }

    @Override
    public List<UserRankDto> getTodayExcerciseKing() {
        try {
            return challengeDao.getTodayExcerciseKing();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<UserRankDto> getTodayMealKing() {
        try {
            return challengeDao.getTodayMealKing();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<UserRankDto> getMonthWeightLossKing() {
        try {
            return challengeDao.getMonthWeightLossKing();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
