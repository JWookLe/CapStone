<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>챌린지 결과</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet" />
    <link href="resources/css/health.css" rel="stylesheet" />
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background: #f7fafd;
            line-height: 1.6;
            padding: 2rem 1rem;
        }
        .result-card {
            max-width: 700px;
            margin: 0 auto;
            background: #fff;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem;
        }
        h1 {
            font-weight: 800;
            font-size: 2.5rem;
            color: #43c6ac;
            letter-spacing: 2px;
            text-align: center;
            margin-bottom: 2rem;
        }
        .challenge-item {
            font-size: 1.4rem;
            font-weight: 600;
            margin-bottom: 2rem;
            color: #191654;
            padding: 1rem 1.5rem;
            background: #e9f7ef;
            border-left: 5px solid #28a745;
            border-radius: 1rem;
        }
        .rank-list > div {
            display: flex;
            align-items: center;
            margin-bottom: 0.8rem;
            font-weight: 700;
            font-size: 1.2rem;
            color: #2a2a72;
        }
        .rank-number {
            min-width: 3rem;
            font-size: 1.3rem;
            color: #43c6ac;
            font-weight: 900;
            margin-right: 0.8rem;
        }
        .username {
            flex-grow: 1;
            color: #191654;
        }
        .calories {
            color: #28a745;
            font-weight: 600;
            margin-left: 1rem;
        }
        .weight-loss-king {
            font-weight: 800;
            font-size: 1.4rem;
            color: #d9534f;
            margin-top: 2rem;
            text-align: center;
        }
        .footer-btn {
            display: flex;
            justify-content: center;
            margin-top: 3rem;
        }
        .btn-home {
            font-size: 1.2rem;
            border-radius: 2rem;
            padding: 0.75rem 3rem;
            background: linear-gradient(90deg, #43c6ac, #191654);
            color: white;
            border: none;
            cursor: pointer;
            transition: background 0.3s ease;
            text-decoration: none;
            display: inline-block;
        }
        .btn-home:hover {
            background: linear-gradient(90deg, #191654, #43c6ac);
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="result-card">
    <h1>챌린지 결과</h1>

    <div class="challenge-item">
        오늘의 운동왕 TOP 3:
        <div class="rank-list">
            <c:choose>
                <c:when test="${not empty exerciseRanks}">
                    <c:forEach var="rank" items="${exerciseRanks}" varStatus="status">
                        <div>
                            <span class="rank-number">${status.index + 1}등</span>
                            <span class="username">${rank.username}</span>
                            <span class="calories">(${rank.value} kcal)</span>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>오늘은 아직 운동왕 기록이 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="challenge-item">
        오늘의 식사왕 TOP 3:
        <div class="rank-list">
            <c:choose>
                <c:when test="${not empty mealRanks}">
                    <c:forEach var="rank" items="${mealRanks}" varStatus="status">
                        <div>
                            <span class="rank-number">${status.index + 1}등</span>
                            <span class="username">${rank.username}</span>
                            <span class="calories">(${rank.value} kcal)</span>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>오늘은 아직 식사왕 기록이 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="challenge-item">
        이달의 감량왕 TOP 3:
        <div class="rank-list">
            <c:choose>
                <c:when test="${not empty weightLossRanks}">
                    <c:forEach var="rank" items="${weightLossRanks}" varStatus="status">
                        <div>
                            <span class="rank-number">${status.index + 1}등</span>
                            <span class="username">${rank.username}</span>
                            <span class="calories">(${rank.value / 100.0} kg 감량)</span>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>이번 달은 아직 감량왕 기록이 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="footer-btn">
        <c:choose>
            <c:when test="${pageContext.request.userPrincipal.name == 'administer1234'}">
                <a href="/adminDashboard" class="btn-home">관리자 대시보드로 돌아가기</a>
            </c:when>
            <c:otherwise>
                <a href="/dashboard" class="btn-home">대시보드로 돌아가기</a>
            </c:otherwise>
        </c:choose>
    </div>

</div>
</body>
</html>
