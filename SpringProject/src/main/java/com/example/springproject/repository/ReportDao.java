package com.example.springproject.repository;

import com.example.springproject.model.Report;
import com.example.springproject.model.ReportedPostSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportDao {

    @Autowired
    private DataSource dataSource;

    // 신고 저장
    public void save(Report report) throws SQLException {
        String sql = "INSERT INTO report (post_id, reporter_id, reason, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, report.getPostId());
            ps.setString(2, report.getReporterId());
            ps.setString(3, report.getReason());
            ps.setTimestamp(4, Timestamp.valueOf(report.getCreatedAt()));
            ps.executeUpdate();
        }
    }

    // 게시글 별 신고 내역 조회
    public List<Report> findByPostId(Long postId) throws SQLException {
        String sql = "SELECT * FROM report WHERE post_id = ? ORDER BY created_at DESC";
        List<Report> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Report report = new Report();
                    report.setId(rs.getLong("id"));
                    report.setPostId(rs.getLong("post_id"));
                    report.setReporterId(rs.getString("reporter_id"));
                    report.setReason(rs.getString("reason"));
                    report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(report);
                }
            }
        }
        return list;
    }

    public void deleteByPostId(Long postId) throws SQLException {
        String sql = "DELETE FROM report WHERE post_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.executeUpdate();
        }
    }
    public List<ReportedPostSummary> getReportedPostSummaries() throws SQLException {
        String summarySql = "SELECT bp.user_id AS reported_user_id, bp.id AS post_id, bp.title AS post_title, COUNT(r.id) AS report_count " +
                "FROM report r " +
                "JOIN board_post bp ON r.post_id = bp.id " +
                "GROUP BY bp.user_id, bp.id, bp.title " +
                "ORDER BY report_count DESC";

        String reasonsSql = "SELECT reason FROM report WHERE post_id = ?";

        List<ReportedPostSummary> summaries = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement summaryPs = conn.prepareStatement(summarySql);
             ResultSet rs = summaryPs.executeQuery()) {

            while (rs.next()) {
                ReportedPostSummary summary = new ReportedPostSummary();
                Long postId = rs.getLong("post_id");

                summary.setReportedUserId(rs.getString("reported_user_id"));
                summary.setPostId(postId);
                summary.setPostTitle(rs.getString("post_title"));
                summary.setReportCount(rs.getInt("report_count"));

                // 신고 사유 목록 가져오기
                try (PreparedStatement reasonPs = conn.prepareStatement(reasonsSql)) {
                    reasonPs.setLong(1, postId);
                    try (ResultSet reasonRs = reasonPs.executeQuery()) {
                        List<String> reasons = new ArrayList<>();
                        while (reasonRs.next()) {
                            reasons.add(reasonRs.getString("reason"));
                        }
                        summary.setReportReasons(reasons);
                    }
                }

                summaries.add(summary);
            }
        }

        return summaries;
    }


}
