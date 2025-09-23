<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>맞춤형 건강 리포트</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/health.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background: #f7fafd;
            line-height: 1.6;
        }
        .report-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 2rem 1rem;
        }
        .report-card {
            width: 100%;
            max-width: 700px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem;
            background: #fff;
        }
        .header-title {
            font-size: 2.5rem;
            font-weight: 800;
            color: #43c6ac;
            letter-spacing: 2px;
        }
        .sub-text {
            font-size: 1.1rem;
            color: #555;
        }
        h2 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 2rem;
            text-align: center;
        }
        .report-item {
            font-size: 1.2rem;
            margin-bottom: 1rem;
        }
        .highlight {
            font-weight: bold;
            color: #d9534f;
        }
        .suggestion {
            background-color: #e9f7ef;
            padding: 15px;
            border-left: 5px solid #28a745;
            margin-top: 2rem;
            font-size: 1.1rem;
        }
        .btn {
            font-size: 1.2rem;
            border-radius: 2rem;
            height: 56px;
        }
        /* progress bar 추가 스타일 */
        .progress {
            height: 25px;
            border-radius: 12.5px;
            background-color: #e9ecef;
            overflow: hidden;
        }
        .progress-bar {
            font-weight: 700;
            line-height: 25px;
            color: #fff;
            text-align: center;
            white-space: nowrap;
            transition: width 0.6s ease;
            background: linear-gradient(90deg, #43c6ac, #191654);
        }
    </style>
</head>
<body>
<div class="report-center">
    <div class="text-center mb-4">
        <div class="header-title">NutriThlet</div>
        <div class="sub-text">나만의 식단과 건강을 흐름처럼 관리하다</div>
    </div>
    <div class="card report-card">
        <h2>${username}님의 맞춤형 건강 리포트</h2>

        <div class="report-item"><strong>성별:</strong> ${gender}</div>
        <div class="report-item"><strong>나이:</strong> ${age}세</div>
        <div class="report-item"><strong>키:</strong> ${height} cm</div>
        <div class="report-item"><strong>몸무게:</strong> ${weight} kg</div>
        <div class="report-item"><strong>기초대사량(BMR):</strong>
            <fmt:formatNumber value="${bmr}" pattern="#0.0" /> kcal
        </div>
        <div class="report-item"><strong>오늘 섭취한 칼로리:</strong> ${totalCalories} kcal</div>
        <div class="report-item">
            <strong>일일 칼로리 목표:</strong>
            <c:choose>
                <c:when test="${targetCalories == 0}">
                    <span style="color: #d9534f;">칼로리를 입력해주세요</span>
                </c:when>
                <c:otherwise>
                    ${targetCalories} kcal
                </c:otherwise>
            </c:choose>
        </div>

        <!-- 칼로리 진행도(progress bar) -->
        <c:if test="${targetCalories > 0}">
            <div class="report-item" style="margin-top:1.5rem;">
                <strong>칼로리 진행도:</strong>
                <%
                    // JSP 스크립틀릿으로 진행도 계산 (서버에서 계산할 수도 있음)
                    double totalCalories = Double.parseDouble(request.getAttribute("totalCalories").toString());
                    double targetCalories = Double.parseDouble(request.getAttribute("targetCalories").toString());
                    double progress = (totalCalories / targetCalories) * 100;
                    if(progress > 100) progress = 100;  // 100% 넘으면 막대는 꽉 채움
                %>
                <div class="progress mt-2">
                    <div class="progress-bar" role="progressbar" style="width: <%=progress%>%;"
                         aria-valuenow="<%=progress%>" aria-valuemin="0" aria-valuemax="100">
                        <fmt:formatNumber value="${totalCalories}" pattern="#0"/> / <fmt:formatNumber value="${targetCalories}" pattern="#0"/> kcal (<%= String.format("%.1f", progress) %>%)
                    </div>
                </div>
            </div>
        </c:if>

        <div class="report-item">
            <strong>칼로리 차이:</strong>
            <span class="highlight">
                <fmt:formatNumber value="${calorieDifference}" pattern="#0.0" /> kcal
            </span>
            <c:choose>
                <c:when test="${calorieDifference > 0}">(섭취 부족)</c:when>
                <c:when test="${calorieDifference < 0}">(과다 섭취)</c:when>
                <c:otherwise>(적정 섭취)</c:otherwise>
            </c:choose>
        </div>

        <div class="suggestion">
            <strong>추천:</strong><br/>
            ${exerciseRecommendation}
        </div>

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
