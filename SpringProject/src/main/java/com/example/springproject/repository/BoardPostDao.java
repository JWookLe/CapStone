package com.example.springproject.repository;

import com.example.springproject.model.BoardPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BoardPostDao {
    @Autowired
    private DataSource dataSource;

    public List<BoardPost> findAll() throws SQLException {
        String sql = "SELECT * FROM board_post ORDER BY id DESC";
        List<BoardPost> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BoardPost post = new BoardPost();
                post.setId(rs.getLong("id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setUserId(rs.getString("user_id"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                post.setViewCount(rs.getInt("view_count"));
                list.add(post);
            }
        }
        return list;
    }

    public BoardPost findById(Long id) throws SQLException {
        String sql = "SELECT * FROM board_post WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BoardPost post = new BoardPost();
                    post.setId(rs.getLong("id"));
                    post.setTitle(rs.getString("title"));
                    post.setContent(rs.getString("content"));
                    post.setUserId(rs.getString("user_id"));
                    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    post.setViewCount(rs.getInt("view_count"));
                    return post;
                }
            }
        }
        return null;
    }

    public void save(BoardPost post) throws SQLException {
        String sql = "INSERT INTO board_post (title, content, user_id) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getUserId());
            ps.executeUpdate();
        }
    }

    public void increaseViewCount(Long id) throws SQLException {
        String sql = "UPDATE board_post SET view_count = view_count + 1 WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM board_post WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
} 