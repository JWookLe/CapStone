package com.example.springproject.model;

import java.util.List;

public class ReportedPostSummary {
    private String reportedUserId;
    private Long postId;
    private String postTitle;
    private int reportCount;
    private List<String> reportReasons; // ✅ 복수 신고 사유


    public String getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(String reportedUserId) { this.reportedUserId = reportedUserId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public List<String> getReportReasons() { return reportReasons; }
    public void setReportReasons(List<String> reportReasons) { this.reportReasons = reportReasons; }

    @Override
    public String toString() {
        return "ReportedPostSummary{" +
                "reportedUserId='" + reportedUserId + '\'' +
                ", postId=" + postId +
                ", postTitle='" + postTitle + '\'' +
                ", reportCount=" + reportCount +
                '}';
    }
}
