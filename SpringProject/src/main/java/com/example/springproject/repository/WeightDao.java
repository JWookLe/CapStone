package com.example.springproject.repository;

import com.example.springproject.model.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WeightDao {

    @Autowired
    private DataSource dataSource;

    // 몸무게 기록 추가
    public void save(Weight weight) throws SQLException {
        String sql = "INSERT INTO weight_records (user_id, date, weight) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, weight.getUserId());
            ps.setDate(2, Date.valueOf(weight.getDate()));
            ps.setDouble(3, weight.getWeight());

            ps.executeUpdate();
        }
    }

    // 특정 사용자의 몸무게 기록 리스트 조회
    public List<Weight> findByUserId(String userId) throws SQLException {
        String sql = "SELECT id, user_id, date, weight FROM weight_records WHERE user_id = ? ORDER BY date";
        List<Weight> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Weight weight = new Weight();
                    weight.setId(rs.getLong("id"));
                    weight.setUserId(rs.getString("user_id"));
                    weight.setDate(rs.getDate("date").toLocalDate());
                    weight.setWeight(rs.getDouble("weight"));
                    list.add(weight);
                }
            }
        }
        return list;
    }

    // 특정 사용자, 특정 날짜 몸무게 기록 조회
    public Weight findByUserIdAndDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT id, user_id, date, weight FROM weight_records WHERE user_id = ? AND date = ?";
        Weight weight = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    weight = new Weight();
                    weight.setId(rs.getLong("id"));
                    weight.setUserId(rs.getString("user_id"));
                    weight.setDate(rs.getDate("date").toLocalDate());
                    weight.setWeight(rs.getDouble("weight"));
                }
            }
        }
        return weight;
    }

    // ID로 몸무게 기록 조회
    public Weight findById(Long id) throws SQLException {
        String sql = "SELECT id, user_id, date, weight FROM weight_records WHERE id = ?";
        Weight weight = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    weight = new Weight();
                    weight.setId(rs.getLong("id"));
                    weight.setUserId(rs.getString("user_id"));
                    weight.setDate(rs.getDate("date").toLocalDate());
                    weight.setWeight(rs.getDouble("weight"));
                }
            }
        }
        return weight;
    }

    // 몸무게 기록 수정 (ID 기준으로 수정)
    public void update(Weight weight) throws SQLException {
        String sql = "UPDATE weight_records SET weight = ?, date = ?, user_id = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, weight.getWeight());
            ps.setDate(2, Date.valueOf(weight.getDate()));
            ps.setString(3, weight.getUserId());
            ps.setLong(4, weight.getId());

            ps.executeUpdate();
        }
    }

    // 몸무게 기록 삭제
    public void deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM weight_records WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
    // 가장 최근 몸무게 조회
    public Weight findLatestByUserId(String userId) throws SQLException {
        String sql = "SELECT id, user_id, date, weight FROM weight_records WHERE user_id = ? ORDER BY date DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Weight weight = new Weight();
                    weight.setId(rs.getLong("id"));
                    weight.setUserId(rs.getString("user_id"));
                    weight.setDate(rs.getDate("date").toLocalDate());
                    weight.setWeight(rs.getDouble("weight"));
                    return weight;
                }
            }
        }
        return null;
    }

}
