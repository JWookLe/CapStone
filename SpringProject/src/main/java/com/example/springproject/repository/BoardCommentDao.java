package com.example.springproject.repository;

import com.example.springproject.model.BoardComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BoardCommentDao {
    @Autowired
    private DataSource dataSource;

    // 계층형 댓글 트리 조회 (depth 계산)
    public List<BoardComment> findByPostId(Long postId) throws SQLException {
        List<BoardComment> result = new ArrayList<>();
        fetchComments(result, postId, null, 0);
        return result;
    }

    private void fetchComments(List<BoardComment> result, Long postId, Long parentId, int depth) throws SQLException {
        String sql = "SELECT * FROM board_comment WHERE post_id = ? AND parent_id " + (parentId == null ? "IS NULL" : "= ?") + " ORDER BY id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            if (parentId != null) ps.setLong(2, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BoardComment comment = new BoardComment();
                    comment.setId(rs.getLong("id"));
                    comment.setPostId(rs.getLong("post_id"));
                    comment.setParentId(rs.getObject("parent_id") == null ? null : rs.getLong("parent_id"));
                    comment.setUserId(rs.getString("user_id"));
                    comment.setContent(rs.getString("content"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comment.setDepth(depth);
                    result.add(comment);
                    // 대댓글 재귀
                    fetchComments(result, postId, comment.getId(), depth + 1);
                }
            }
        }
    }

    public void save(BoardComment comment) throws SQLException {
        String sql = "INSERT INTO board_comment (post_id, parent_id, user_id, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, comment.getPostId());
            if (comment.getParentId() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, comment.getParentId());
            }
            ps.setString(3, comment.getUserId());
            ps.setString(4, comment.getContent());
            ps.executeUpdate();
        }
    }

    public void deleteByPostId(Long postId) throws SQLException {
        String sql = "DELETE FROM board_comment WHERE post_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.executeUpdate();
        }
    }
} 