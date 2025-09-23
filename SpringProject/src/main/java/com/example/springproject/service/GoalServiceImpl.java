 package com.example.springproject.service;
import com.example.springproject.model.Goal;
import com.example.springproject.model.User;
import com.example.springproject.repository.GoalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.SQLException;
import java.time.LocalDate;

@Service
public class GoalServiceImpl implements GoalService {
    @Autowired
    private GoalDao goalDao;

    @Override
    public void save(Goal goal, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        goal.setUserId(user.getId());
        try {
            goalDao.save(goal);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Goal> getGoals(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try {
            return goalDao.findByUser(user.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Goal getCurrentGoal(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try {
            List<Goal> goals = goalDao.findByUserAndDate(user.getId(), LocalDate.now());
            if (goals.isEmpty()) return null;
            return goals.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Goal goal) {
        try {
            goalDao.deleteById(goal.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Goal> getGoalsByDate(User user, LocalDate date) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try {
            return goalDao.findByUserAndDate(user.getId(), date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Goal getCurrentGoalByDate(User user, LocalDate date) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try {
            List<Goal> goals = goalDao.findByUserAndDate(user.getId(), date);
            if (goals.isEmpty()) return null;
            return goals.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching goals for user: " + user.getId(), e);
        }
    }
   @Override
    public int getTargetCaloriesByUserAndDate(User user, LocalDate date) {
        Goal goal = getCurrentGoalByDate(user, date);  // 기존 메서드 재활용
        if (goal != null) {
            return goal.getTargetCalories();  // Goal 객체의 목표 칼로리 반환
        }
        return 0;  // 없으면 0 반환 (또는 적당한 기본값)
    }

}




