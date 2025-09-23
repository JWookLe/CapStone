package com.example.springproject.service;

import com.example.springproject.repository.UserRankDto;

import java.util.List;

public interface ChallengeService {
    List<UserRankDto> getTodayExcerciseKing();
    List<UserRankDto> getTodayMealKing();
    List<UserRankDto> getMonthWeightLossKing();

}
