package com.example.springproject.repository;

import com.example.springproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Repository
public class UserDao {
    @Autowired
    private DataSource dataSource;

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setHeight(rs.getBigDecimal("height"));
                    user.setGender(rs.getString("gender"));
                    user.setBanned(rs.getBoolean("is_banned"));
                    Date birth = rs.getDate("birth_date");

                    if (birth != null) {
                        user.setBirthDate(birth.toLocalDate());
                    }
                    return user;
                }
            }
        }
        return null;
    }

    public User findById(String id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setHeight(rs.getBigDecimal("height"));
                    user.setGender(rs.getString("gender"));
                    user.setBanned(rs.getBoolean("is_banned"));
                    Date birth = rs.getDate("birth_date");
                    if (birth != null) {
                        user.setBirthDate(birth.toLocalDate());
                    }
                    return user;
                }
            }
        }
        return null;
    }
    public User findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM user WHERE role = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setHeight(rs.getBigDecimal("height"));
                    user.setGender(rs.getString("gender"));
                    user.setRole(rs.getString("role"));  // role 필드 추가
                    user.setBanned(rs.getBoolean("is_banned"));
                    Date birth = rs.getDate("birth_date");
                    if (birth != null) {
                        user.setBirthDate(birth.toLocalDate());
                    }
                    return user;
                }
            }
        }
        return null;
    }

    public void save(User user) throws SQLException {
        String sql = "INSERT INTO user (id, username, password, height, gender, birth_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());

            if (user.getHeight() != null) {
                ps.setBigDecimal(4, user.getHeight());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }

            ps.setString(5, user.getGender());

            if (user.getBirthDate() != null) {
                ps.setDate(6, java.sql.Date.valueOf(user.getBirthDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.executeUpdate();
        }
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET username = ?, password = ?, height = ?, gender = ?, birth_date = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());

            if (user.getHeight() != null) {
                ps.setBigDecimal(3, user.getHeight());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }

            ps.setString(4, user.getGender());

            if (user.getBirthDate() != null) {
                ps.setDate(5, java.sql.Date.valueOf(user.getBirthDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setString(6, user.getId());
            ps.executeUpdate();
        }
    }

    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public void banUserById(String userId) throws SQLException {
        String sql = "UPDATE user SET is_banned = TRUE WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

}
