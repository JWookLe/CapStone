<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>신고 내역</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;700;900&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background: #f6f9fc;
            margin: 0;
            padding: 0;
        }
        .report-center {
            padding: 3rem 1rem;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .report-card {
            width: 100%;
            max-width: 1200px;
            background: #fff;
            border-radius: 1.8rem;
            padding: 3rem;
            box-shadow: 0 12px 24px rgba(0,0,0,0.1);
        }
        .logo {
            text-align: center;
            margin-bottom: 1.5rem;
        }
        .logo .title {
            font-size: 2.8rem;
            font-weight: 900;
            color: #43c6ac;
        }
        .logo .subtitle {
            font-size: 1.1rem;
            color: #666;
        }
        h2 {
            font-size: 2rem;
            font-weight: 800;
            color: #191654;
            text-align: center;
            margin-bottom: 2rem;
        }
        .section-title {
            font-size: 1.3rem;
            font-weight: 700;
            color: #43c6ac;
            margin-bottom: 1rem;
        }
        .table {
            font-size: 1.05rem;
            background-color: #f8f9fa;
            border-radius: 1rem;
            overflow: hidden;
            margin-bottom: 1.5rem;
        }
        .table thead {
            background-color: #f0f2f5;
            font-weight: 700;
            color: #444;
        }
        .table td, .table th {
            text-align: center;
            vertical-align: middle;
            padding: 0.8rem;
        }
        .btn-ban {
            padding: 0.4rem 1rem;
            font-size: 0.95rem;
            font-weight: 600;
            color: #dc3545;
            border: 2px solid #dc3545;
            background: transparent;
            border-radius: 2rem;
            transition: 0.3s ease;
        }
        .btn-ban:hover {
            background-color: #dc3545;
            color: white;
        }
        .btn-view {
            padding: 0.4rem 1rem;
            font-size: 0.95rem;
            font-weight: 600;
            color: #007bff;
            border: 2px solid #007bff;
            background: transparent;
            border-radius: 2rem;
            transition: 0.3s ease;
        }
        .btn-view:hover {
            background-color: #007bff;
            color: white;
        }
        ul.reason-list {
            padding-left: 1rem;
            text-align: left;
            margin: 0;
        }
        ul.reason-list li {
            margin-bottom: 0.3rem;
        }
    </style>
</head>
<body>
<div class="report-center">
    <div class="logo">
        <div class="title">관리자 신고 리포트</div>
        <div class="subtitle">신고당한 사용자 및 게시글 관리</div>
    </div>

    <div class="report-card">
        <h2>신고 내역</h2>

        <div class="section-title">🚨 신고당한 게시글 목록</div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>게시글 ID</th>
                <th>게시글 제목</th>
                <th>유저 ID</th>
                <th>신고 횟수</th>
                <th>신고 사유</th>
                <th>게시글 보기</th>
                <th>회원 박탈</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="summary" items="${reportedPosts}">
                <tr>
                    <td>${summary.postId}</td>
                    <td>${summary.postTitle}</td>
                    <td>${summary.reportedUserId}</td>
                    <td>${summary.reportCount}</td>
                    <td>
                        <ul class="reason-list">
                            <c:forEach var="reason" items="${summary.reportReasons}">
                                <li>${reason}</li>
                            </c:forEach>
                        </ul>
                    </td>
                    <td>
                        <a href="/bulletinBoard/view?id=${summary.postId}" class="btn-view">게시글 보기</a>
                    </td>
                    <td>
                        <form method="post" action="/banUser">
                            <input type="hidden" name="userId" value="${summary.reportedUserId}" />
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="btn-ban">회원 박탈</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

    </div>
    <c:if test="${not empty message}">
        <div id="popupMessage" style="
        position: fixed;
        top: 50%; left: 50%;
        transform: translate(-50%, -50%);
        background: #f8d7da;
        color: #721c24;
        padding: 30px 40px;
        border: 2px solid #f5c6cb;
        border-radius: 8px;
        box-shadow: 0 0 15px rgba(0,0,0,0.3);
        font-size: 1.5rem;
        z-index: 9999;
        text-align: center;
        max-width: 80%;
        ">
                ${message}
            <br><br>
            <button onclick="document.getElementById('popupMessage').style.display='none'"
                    style="
                padding: 8px 20px;
                font-size: 1rem;
                background-color: #721c24;
                color: white;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                ">닫기</button>
        </div>
    </c:if>

    <div class="footer-btn" style="text-align:center; margin-top: 2rem;">
        <c:choose>
            <c:when test="${pageContext.request.userPrincipal.name == 'administer1234'}">
                <a href="/adminDashboard" class="btn-home" style="padding:0.6rem 1.2rem; background:#43c6ac; color:#fff; border-radius:1rem; text-decoration:none; font-weight:bold;">관리자 대시보드로 돌아가기</a>
            </c:when>
            <c:otherwise>
                <a href="/dashboard" class="btn-home" style="padding:0.6rem 1.2rem; background:#43c6ac; color:#fff; border-radius:1rem; text-decoration:none; font-weight:bold;">대시보드로 돌아가기</a>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
