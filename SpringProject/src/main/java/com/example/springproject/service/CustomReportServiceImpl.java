package com.example.springproject.service;

import com.example.springproject.model.CustomReport;
import com.example.springproject.model.User;
import com.example.springproject.model.Weight;
import com.example.springproject.repository.UserDao;
import com.example.springproject.repository.WeightDao;
import com.example.springproject.repository.MealDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class CustomReportServiceImpl implements CustomReportService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private WeightDao weightDao;

    @Autowired
    private MealDao mealDao;

    @Override
    public CustomReport getCustomReportData(String userId) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return null;
            }

            int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();

            List<Weight> weightList = weightDao.findByUserId(userId);
            if (weightList.isEmpty()) {
                return null;
            }
            Weight latestWeight = weightList.get(weightList.size() - 1);

            double bmr;
            if ("M".equalsIgnoreCase(user.getGender())) {
                bmr = 10 * latestWeight.getWeight() + 6.25 * user.getHeight().doubleValue() - 5 * age + 5;
            } else {
                bmr = 10 * latestWeight.getWeight() + 6.25 * user.getHeight().doubleValue() - 5 * age - 161;
            }

            double totalCalories = mealDao.getTodayCaloriesByUserId(userId, LocalDate.now());

            double recommendedCalories = bmr * 1.2;

            double diffCalories = recommendedCalories - totalCalories;

            String exerciseRecommendation;
            if (diffCalories > 200) {
                exerciseRecommendation = "운동량을 늘려 칼로리 소모를 더 하세요.";
            } else if (diffCalories < -200) {
                exerciseRecommendation = "운동 강도를 줄이고 식단을 조절하세요.";
            } else {
                exerciseRecommendation = "현재 상태를 유지하세요.";
            }

            CustomReport report = new CustomReport();
            report.setUser(user);
            report.setLatestWeight(latestWeight.getWeight());
            report.setBmr(bmr);
            report.setTotalCalories(totalCalories);
            report.setRecommendedCalories(recommendedCalories);
            report.setCalorieDifference(diffCalories);
            report.setExerciseRecommendation(exerciseRecommendation);

            return report;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
