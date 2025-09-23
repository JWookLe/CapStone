<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>월간 리포트</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/health.css" rel="stylesheet">
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
            max-width: 860px;
            background: #fff;
            border-radius: 1.8rem;
            padding: 3rem 3rem 3.5rem;
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
        .date {
            font-size: 1.3rem;
            text-align: center;
            margin-bottom: 1.5rem;
            color: #222;
        }
        h2 {
            font-size: 2.1rem;
            font-weight: 800;
            color: #191654;
            text-align: center;
            margin-bottom: 2rem;
        }
        .section-title {
            font-size: 1.3rem;
            font-weight: 700;
            color: #43c6ac;
            margin-top: 2rem;
            margin-bottom: 0.8rem;
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
        .fw-bold {
            font-weight: 700;
            font-size: 1.1rem;
            color: #222;
        }
        .btn-dashboard {
            display: inline-block;
            margin-top: 2rem;
            padding: 0.7rem 2rem;
            font-size: 1.1rem;
            font-weight: 600;
            color: #43c6ac;
            background-color: transparent;
            border: 2px solid #43c6ac;
            border-radius: 2rem;
            text-decoration: none;
            transition: all 0.3s ease;
        }
        .btn-dashboard:hover {
            background-color: #43c6ac;
            color: white;
            text-decoration: none;
        }
        @media (max-width: 768px) {
            .report-card { padding: 2rem; }
            h2 { font-size: 1.6rem; }
            .logo .title { font-size: 2rem; }
            .section-title { font-size: 1.15rem; }
            .table { font-size: 0.95rem; }
        }
    </style>
</head>
<body>
<div class="report-center">
    <div class="logo">
        <div class="title">NutriThlet</div>
        <div class="subtitle">나만의 식단과 건강을 흐름처럼 관리하다</div>
    </div>
    <div class="date">${todayStr}</div>

    <div class="report-card">
        <h2>${startOfMonth} ~ ${endOfMonth} 월간 리포트</h2>

        <div class="section-title">🍽 식단</div>
        <table class="table table-striped">
            <thead>
            <tr><th>날짜</th><th>식사</th><th>음식명</th><th>칼로리</th></tr>
            </thead>
            <tbody>
            <c:forEach var="meal" items="${meals}">
                <tr>
                    <td>${meal.date}</td>
                    <td>
                        <c:choose>
                            <c:when test="${meal.mealType == 'BREAKFAST'}">아침</c:when>
                            <c:when test="${meal.mealType == 'LUNCH'}">점심</c:when>
                            <c:otherwise>저녁</c:otherwise>
                        </c:choose>
                    </td>
                    <td>${meal.foodName}</td>
                    <td>${meal.calories} kcal</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="fw-bold text-center">총 섭취 칼로리: ${totalCalories} kcal</div>
        <div class="fw-bold text-center mb-4">평균 섭취 칼로리: <fmt:formatNumber value="${avgCalories}" type="number" maxFractionDigits="2"/> kcal</div>

        <div class="section-title">🏋 운동</div>
        <table class="table table-striped">
            <thead><tr><th>날짜</th><th>운동명</th><th>세부 운동</th><th>소모 칼로리</th></tr></thead>
            <tbody>
            <c:forEach var="ex" items="${exercises}">
                <tr>
                    <td>${ex.date}</td>
                    <td>${ex.exerciseName}</td>
                    <td>${ex.detailExerciseName}</td>
                    <td>${ex.burnedCalories} kcal</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="fw-bold text-center">총 소모 칼로리: ${totalBurned} kcal</div>
        <div class="fw-bold text-center">평균 소모 칼로리: <fmt:formatNumber value="${avgBurned}" type="number" maxFractionDigits="2"/> kcal</div>
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
</div>
</body>
</html>