<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--@elvariable id="goalForm" type="com.example.springproject.model.Goal"--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>칼로리 목표 설정</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet" />
    <link href="resources/css/health.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet" />
    <style>
        body { font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif; }
        .goal-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .goal-card {
            width: 100%;
            max-width: 600px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem 2.5rem 2rem 2.5rem;
        }
        .goal-card h2 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 2rem;
        }
        .form-control {
            height: 56px;
            font-size: 1.2rem;
            border-radius: 1.5rem;
            margin-bottom: 1.2rem;
        }
        .btn-warning {
            font-size: 1.2rem;
            border-radius: 2rem;
            height: 56px;
        }
        .table {
            border-radius: 1.5rem;
            overflow: hidden;
            margin-top: 1.5rem;
        }
        .fw-bold {
            font-size: 1.2rem;
        }
    </style>
</head>
<body>
<div class="goal-center">
    <div class="card goal-card">
        <h2 class="text-center">칼로리 목표 설정 (${today})</h2>
        <c:if test="${not empty error}">
            <div class="alert alert-danger text-center">${error}</div>
        </c:if>
        <div class="text-end mb-2">
            <form id="goalSearchForm" method="get" action="/goal" class="d-inline">
                <input type="date" name="searchDate" id="goalSearchDate" class="form-control d-inline" style="width:180px;display:inline-block;" value="${today}" required>
                <button type="submit" class="btn btn-outline-warning ms-2">조회</button>
            </form>
        </div>
        <form:form method="POST" modelAttribute="goalForm" class="row g-2 align-items-end mb-3" id="goalForm">
            <div class="col-md-8">
                <form:input path="targetCalories" type="number" class="form-control" placeholder="목표 칼로리 (kcal)" required="required" />
            </div>
            <div class="col-md-4">
                <form:input path="date" type="date" class="form-control" value="${today}" required="required" />
            </div>
            <div class="col-12 mt-2">
                <button class="btn btn-warning w-100">등록</button>
            </div>
        </form:form>

        <h4 class="mt-4">현재 목표</h4>
        <c:choose>
            <c:when test="${not empty currentGoal}">
                <div class="alert alert-info text-center">
                    <div>목표: <b>${currentGoal.targetCalories} kcal</b></div>
                    <div>날짜: <b>${currentGoal.date}</b></div>
                    <form method="post" action="goal/delete" style="display:inline;">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        <button type="submit" class="btn btn-danger btn-sm mt-2">목표 삭제</button>
                    </form>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-secondary text-center">설정된 목표가 없습니다.</div>
            </c:otherwise>
        </c:choose>
        <h4 class="mt-4">목표 이력</h4>
        <table class="table table-bordered text-center">
            <thead><tr><th>날짜</th><th>목표 칼로리</th></tr></thead>
            <tbody>
            <c:forEach var="goal" items="${goals}">
                <tr>
                    <td>${goal.date}</td>
                    <td>${goal.targetCalories}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="d-flex justify-content-center mt-3">
            <a href="dashboard" class="btn btn-outline-secondary">대시보드로</a>
        </div>
    </div>
</div>
<script>
document.getElementById('goalForm').addEventListener('submit', function(e) {
  const cal = this.targetCalories.value.trim();
  const date = this.date.value.trim();
  if (!cal || !date) {
    alert('모든 정보를 입력하세요.');
    e.preventDefault();
  }
});
</script>
</body>
</html>
