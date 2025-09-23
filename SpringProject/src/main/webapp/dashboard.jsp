<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>나만의 건강 식단 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="resources/css/health.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet" />
    <style>
        body {
            font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif;
        }
        .dashboard-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .dashboard-header {
            width: 100%;
            max-width: 700px;
            margin: 1.5rem auto;
            text-align: center;
            position: relative;
        }
        .title-text {
            font-size: 2.5rem;
            font-weight: 800;
            color: #43c6ac;
            letter-spacing: 2px;
        }
        .subtitle-text {
            font-size: 1.1rem;
            color: #555;
        }
        .date-text {
            font-size: 1.5rem;
            color: #222;
            margin-top: 0.5rem;
        }
        .hamburger-btn {
            position: absolute;
            top: 50%;
            right: 0;
            transform: translateY(-50%);
            border: none;
            background: transparent;
            cursor: pointer;
            padding: 0.25rem 0.5rem;
        }
        .hamburger-btn:focus {
            outline: none;
        }
        .hamburger-icon {
            width: 24px;
            height: 2px;
            background-color: #333;
            display: block;
            position: relative;
        }
        .hamburger-icon::before,
        .hamburger-icon::after {
            content: '';
            width: 24px;
            height: 2px;
            background-color: #333;
            position: absolute;
            left: 0;
            transition: 0.3s;
        }
        .hamburger-icon::before {
            top: -7px;
        }
        .hamburger-icon::after {
            top: 7px;
        }
        #floatingMenu {
            position: absolute;
            top: 100%;
            right: 0;
            margin-top: 0.25rem;
            min-width: 180px;
            background-color: white;
            border: 1px solid #ddd;
            box-shadow: 0 2px 8px rgb(0 0 0 / 0.15);
            border-radius: 4px;
            display: none;
            z-index: 1050;
        }
        #floatingMenu.show {
            display: block;
        }
        #floatingMenu ul {
            list-style: none;
            margin: 0;
            padding: 0;
            text-align: right;
        }
        #floatingMenu ul li {
            border-bottom: 1px solid #eee;
        }
        #floatingMenu ul li:last-child {
            border-bottom: none;
        }
        #floatingMenu ul li a,
        #floatingMenu ul li button {
            display: block;
            padding: 10px 15px;
            text-decoration: none;
            color: #333;
            font-weight: 500;
            background: none;
            border: none;
            width: 100%;
            text-align: right;
            cursor: pointer;
        }
        #floatingMenu ul li a:hover,
        #floatingMenu ul li button:hover {
            background-color: #f1f1f1;
        }
    </style>
</head>
<body>
<div class="dashboard-center">
    <div class="dashboard-header">
        <div class="title-text">NutriThlet</div>
        <div class="subtitle-text">나만의 식단과 건강을 흐름처럼 관리하다</div>
        <div class="date-text">${todayStr}</div>
        <button id="hamburgerBtn" class="hamburger-btn" aria-haspopup="true" aria-expanded="false" aria-controls="floatingMenu" aria-label="메뉴 열기">
            <span class="hamburger-icon"></span>
        </button>
        <nav id="floatingMenu" aria-hidden="true" role="menu">
            <ul>
                <li><a href="profile" role="menuitem" tabindex="-1">내 정보</a></li>
                <li><a href="/dashboard/weekly-report" role="menuitem" tabindex="-1">주간 리포트</a></li>
                <li><a href="monthlyReport" role="menuitem" tabindex="-1">월간 리포트</a></li>
                <li><a href="customReport" role="menuitem" tabindex="-1">맞춤형 리포트</a></li>
                <li><a href="weightChange" role="menuitem" tabindex="-1">체중 변화</a></li>
                <li><a href="bulletinBoard" role="menuitem" tabindex="-1">게시판</a></li>
                <li><a href="challenge" role="menuitem" tabindex="-1">챌린지</a></li>
                <li>
                    <form id="logoutForm" method="POST" action="logout" class="m-0 p-0" role="none">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        <button type="submit" role="menuitem" tabindex="-1">로그아웃</button>
                    </form>
                </li>
            </ul>
        </nav>
    </div>

    <jsp:include page="calendar.jsp" />

    <div class="row justify-content-center w-100 px-3">
        <div class="col-lg-3 col-md-4 col-sm-6 mb-4">
            <div class="card shadow h-100">
                <div class="card-header bg-primary text-white text-center">오늘의 식단</div>
                <div class="card-body" id="todayMealsCard">
                    <c:forEach var="meal" items="${todayMeals}">
                        <div class="d-flex justify-content-between">
                            <span>
                                <c:choose>
                                    <c:when test="${meal.mealType == 'BREAKFAST'}">아침</c:when>
                                    <c:when test="${meal.mealType == 'LUNCH'}">점심</c:when>
                                    <c:otherwise>저녁</c:otherwise>
                                </c:choose>
                                - ${meal.foodName}
                            </span>
                            <span>${meal.calories} kcal</span>
                        </div>
                    </c:forEach>
                    <hr>
                    <div class="fw-bold text-center" id="todayTotalCalories">총 섭취: ${todayTotalCalories} kcal</div>
                    <div class="d-flex justify-content-center">
                        <a href="meals" class="btn btn-outline-primary btn-sm mt-2">식단 더보기</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-3 col-md-4 col-sm-6 mb-4">
            <div class="card shadow h-100">
                <div class="card-header bg-success text-white text-center">오늘의 운동</div>
                <div class="card-body" id="todayExercisesCard">
                    <c:forEach var="ex" items="${todayExercises}">
                        <div class="d-flex justify-content-between">
                            <span>${ex.exerciseName} (${ex.detailExerciseName})</span>
                            <span>${ex.burnedCalories} kcal</span>
                        </div>
                    </c:forEach>
                    <hr>
                    <div class="fw-bold text-center" id="todayTotalBurned">총 소모: ${todayTotalBurned} kcal</div>
                    <div class="d-flex justify-content-center">
                        <a href="exercises" class="btn btn-outline-success btn-sm mt-2">운동 더보기</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-3 col-md-4 col-sm-6 mb-4">
            <div class="card shadow h-100">
                <div class="card-header bg-warning text-dark text-center">칼로리 목표</div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty currentGoal}">
                            <div class="text-center">목표: <span class="fw-bold">${currentGoal.targetCalories} kcal</span></div>
                            <div class="text-center">섭취-소모: <span class="fw-bold">${todayTotalCalories - todayTotalBurned} kcal</span></div>
                            <c:choose>
                                <c:when test="${todayTotalCalories - todayTotalBurned > currentGoal.targetCalories}">
                                    <div class="alert alert-danger mt-2 text-center">⚠️ 목표 초과!</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="alert alert-success mt-2 text-center">목표 이내! Good!</div>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center">아직 목표가 설정되지 않았습니다.</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="d-flex justify-content-center">
                        <a href="goal" class="btn btn-outline-warning btn-sm mt-2">목표 설정</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const hamburgerBtn = document.getElementById('hamburgerBtn');
    const floatingMenu = document.getElementById('floatingMenu');
    hamburgerBtn.addEventListener('click', () => {
        const isShown = floatingMenu.classList.toggle('show');
        hamburgerBtn.setAttribute('aria-expanded', isShown);
        floatingMenu.setAttribute('aria-hidden', !isShown);
    });
    document.addEventListener('click', (e) => {
        if (!hamburgerBtn.contains(e.target) && !floatingMenu.contains(e.target)) {
            floatingMenu.classList.remove('show');
            hamburgerBtn.setAttribute('aria-expanded', false);
            floatingMenu.setAttribute('aria-hidden', true);
        }
    });
</script>
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
