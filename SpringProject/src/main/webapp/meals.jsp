<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--@elvariable id="mealForm" type="com.example.springproject.model.Meal"--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>식단 관리</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet" />
    <link href="resources/css/health.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet" />
    <style>
        body { font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif; }
        .meals-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .meals-card {
            width: 100%;
            max-width: 600px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem 2.5rem 2rem 2.5rem;
        }
        .meals-card h2 {
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
        .btn-primary {
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
<div class="meals-center">
    <div class="card meals-card">
        <h2 class="text-center">오늘의 식단 (${today})</h2>
        <c:if test="${not empty error}">
            <div class="alert alert-danger text-center">${error}</div>
        </c:if>
        <div class="text-end mb-2">
            <form id="mealSearchForm" method="get" action="/meals" class="d-inline">
                <input type="date" name="searchDate" id="mealSearchDate" class="form-control d-inline" style="width:180px;display:inline-block;" value="${today}" required>
                <button type="submit" class="btn btn-outline-primary ms-2">조회</button>
            </form>
        </div>
        <form:form method="POST" modelAttribute="mealForm" class="row g-2 align-items-end mb-3" id="mealForm">
            <div class="col-md-4">
                <form:input path="foodName" class="form-control" placeholder="음식명" required="required" />
            </div>
            <div class="col-md-2">
                <form:input path="calories" type="number" class="form-control" placeholder="kcal" required="required" />
            </div>
            <div class="col-md-3">
                <form:input path="date" type="date" class="form-control" value="${today}" required="required" />
            </div>
            <div class="col-md-3">
                <form:select path="mealType" class="form-control" required="required">
                    <form:option value="">식사 시간</form:option>
                    <form:option value="BREAKFAST">아침</form:option>
                    <form:option value="LUNCH">점심</form:option>
                    <form:option value="DINNER">저녁</form:option>
                </form:select>
            </div>
            <div class="col-12 mt-2">
                <button class="btn btn-primary w-100">등록</button>
            </div>
        </form:form>

        <c:forEach var="type" items="${['BREAKFAST','LUNCH','DINNER']}">
            <h4 class="text-center mt-4">
                <c:choose>
                    <c:when test="${type == 'BREAKFAST'}">🍽 아침</c:when>
                    <c:when test="${type == 'LUNCH'}">🍱 점심</c:when>
                    <c:otherwise>🍜 저녁</c:otherwise>
                </c:choose>
            </h4>
            <table class="table table-striped text-center">
                <thead>
                <tr><th>음식명</th><th>칼로리</th><th>날짜</th><th></th></tr>
                </thead>
                <tbody>
                <c:forEach var="meal" items="${meals}">
                    <c:if test="${meal.mealType.toString() == type}">
                        <tr>
                            <td>${meal.foodName}</td>
                            <td>${meal.calories}</td>
                            <td>${meal.date}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/meals/delete/${meal.id}"
                                   class="btn btn-danger btn-sm"
                                   onclick="return confirm('삭제하시겠습니까?');">삭제</a>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </c:forEach>

        <div class="fw-bold text-center mt-3">총 섭취 칼로리: ${totalCalories} kcal</div>
        <div class="d-flex justify-content-center mt-3">
            <a href="https://www.dietshin.com/calorie/calorie_main.asp" target="_blank" class="btn btn-info me-2">음식 칼로리 알아보기</a>
            <a href="dashboard" class="btn btn-outline-secondary">대시보드로</a>
        </div>
    </div>
</div>
<script>
document.getElementById('mealForm').addEventListener('submit', function(e) {
  const food = this.foodName.value.trim();
  const cal = this.calories.value.trim();
  const date = this.date.value.trim();
  const mealType = this.mealType.value.trim();
  if (!food || !cal || !date || !mealType) {
    alert('모든 정보를 입력하세요.');
    e.preventDefault();
  }
});
</script>
</body>
</html>
