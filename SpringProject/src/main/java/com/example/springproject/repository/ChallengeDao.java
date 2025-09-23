package com.example.springproject.repository;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

@Repository
public class ChallengeDao {

    private final DataSource dataSource;

    public ChallengeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 오늘의 운동왕 TOP3 조회
    public List<UserRankDto> getTodayExcerciseKing() throws SQLException {
        String sql = "SELECT u.username, SUM(e.burnedCalories) AS total_calories " +
                "FROM exercise e " +
                "JOIN user u ON e.user_id = u.id " +
                "WHERE e.date = ? " +
                "GROUP BY u.username " +
                "ORDER BY total_calories DESC " +
                "LIMIT 3";

        List<UserRankDto> topList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    int calories = rs.getInt("total_calories");
                    topList.add(new UserRankDto(username, calories));
                }
            }
        }

        if (topList.isEmpty()) return null;
        return topList;
    }

    // 오늘의 식사왕 TOP3 조회
    public List<UserRankDto> getTodayMealKing() throws SQLException {
        String sql = "SELECT u.username, SUM(m.calories) AS total_calories " +
                "FROM meal m " +
                "JOIN user u ON m.user_id = u.id " +
                "WHERE m.date = ? " +
                "GROUP BY u.username " +
                "ORDER BY total_calories DESC " +
                "LIMIT 3";

        List<UserRankDto> mealList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    int calories = rs.getInt("total_calories");
                    mealList.add(new UserRankDto(username, calories));
                }
            }
        }

        if (mealList.isEmpty()) return null;
        return mealList;
    }

    // 이달의 감량왕 TOP3 조회
    public List<UserRankDto> getMonthWeightLossKing() throws SQLException {
        YearMonth thisMonth = YearMonth.now();
        LocalDate start = thisMonth.atDay(1);
        LocalDate end = thisMonth.atEndOfMonth();

        String sql = "SELECT u.username, (MIN(w.weight) - MAX(w.weight)) AS weight_loss " +
                "FROM weight_records w JOIN user u ON w.user_id = u.id " +
                "WHERE w.date BETWEEN ? AND ? " +
                "GROUP BY u.username " +
                "HAVING weight_loss < 0 " + // 체중 감소한 사람만
                "ORDER BY weight_loss ASC " + // 감소량이 가장 큰 순서 (음수이므로 ASC)
                "LIMIT 3";

        List<UserRankDto> topList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(start));
            pstmt.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    double weightLoss = rs.getDouble("weight_loss"); // 음수값
                    // 절대값으로 변환, *100하여 소수점 처리 후 int 변환 (ex. 1.23kg → 123)
                    topList.add(new UserRankDto(username, (int) Math.round(Math.abs(weightLoss * 100))));
                }
            }
        }

        if (topList.isEmpty()) return null;
        return topList;
    }

}
