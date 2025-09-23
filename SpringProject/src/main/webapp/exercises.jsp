<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--@elvariable id="exerciseForm" type="com.example.springproject.model.Exercise"--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>운동 관리</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/health.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif; }
        .exercises-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .exercises-card {
            width: 100%;
            max-width: 600px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem;
        }
        .form-control {
            height: 56px;
            font-size: 1.2rem;
            border-radius: 1.5rem;
            margin-bottom: 1.2rem;
        }
        .btn-success {
            font-size: 1.2rem;
            border-radius: 2rem;
            height: 56px;
        }
    </style>
    <script>
        const exerciseMap = {
            "가슴": ["벤치프레스", "체스트프레스", "딥스", "덤벨프레스", "스미스머신벤치프레스", "케이블 크로스오버", "펙덱 플라이", "덤벨 플라이", "디클라인 머신 프레스", "푸쉬업"],
            "등": ["랫풀다운", "바벨로우", "시티드로우", "풀업", "덤벨로우", "T바로우", "머신로우", "슈러그", "업라이트 로우", "데드리프트", "백 익스텐션", "하이로우"],
            "하체": ["스쿼트", "레그프레스", "런지", "레그 익스텐션", "불가리안 스플릿 스쿼트", "레그 컬", "카프 레이즈"],
            "이두": ["덤벨 컬", "바벨 컬", "해머 컬", "케이블 컬"],
            "삼두": ["푸쉬다운", "딥스", "트라이셉스 익스텐션", "클로즈그립 벤치프레스", "덤벨 킥백", "스컬크러셔"],
            "어깨": ["아놀드 프레스", "벤트오버 리어 델트 레이즈", "케이블 레터럴 레이즈", "페이스 풀", "프론트 레이즈", "밀리터리 프레스", "리버스 펙덱 플라이", "사이드 레터럴 레이즈"]
        };

        const exerciseCalories = {
            "벤치프레스": 150,
            "덤벨프레스": 140,
            "스미스머신벤치프레스": 130,
            "체스트프레스": 120,
            "딥스": 100,
            "케이블 크로스오버": 110,
            "펙덱 플라이": 105,
            "덤벨 플라이": 115,
            "디클라인 머신 프레스": 125,
            "푸쉬업": 100,
            "스쿼트": 180,
            "레그프레스": 160,
            "런지": 130,
            "레그 익스텐션": 90,
            "불가리안 스플릿 스쿼트": 120,
            "레그 컬": 85,
            "카프 레이즈": 70,
            "랫풀다운": 140,
            "바벨로우": 150,
            "시티드로우": 135,
            "풀업": 160,
            "덤벨로우": 145,
            "T바로우": 150,
            "머신로우": 130,
            "슈러그": 95,
            "업라이트 로우": 115,
            "데드리프트": 200,
            "백 익스텐션": 90,
            "하이로우": 130,
            "덤벨 컬": 80,
            "바벨 컬": 85,
            "해머 컬": 90,
            "케이블 컬": 95,
            "푸쉬다운": 100,
            "트라이셉스 익스텐션": 95,
            "클로즈그립 벤치프레스": 120,
            "덤벨 킥백": 85,
            "스컬크러셔": 90,
            "아놀드 프레스": 110,
            "벤트오버 리어 델트 레이즈": 95,
            "케이블 레터럴 레이즈": 100,
            "페이스 풀": 90,
            "프론트 레이즈": 90,
            "밀리터리 프레스": 120,
            "리버스 펙덱 플라이": 95,
            "사이드 레터럴 레이즈": 95
        };

        function updateDetailExercises() {
            const category = document.getElementById("exerciseName").value;
            const detailSelect = document.getElementById("detailExerciseName");
            const calorieInput = document.querySelector("input[name='burnedCalories']");

            detailSelect.innerHTML = "";

            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.disabled = true;
            defaultOption.selected = true;
            defaultOption.textContent = "세부 운동 선택";
            detailSelect.appendChild(defaultOption);

            if (exerciseMap[category]) {
                calorieInput.disabled = true;
                exerciseMap[category].forEach(function (ex) {
                    const opt = document.createElement("option");
                    opt.value = ex;
                    opt.text = ex;
                    detailSelect.appendChild(opt);
                });
            } else {
                calorieInput.value = "";
                calorieInput.disabled = true;
            }
        }

        function validateForm(event) {
            const category = document.getElementById("exerciseName").value;
            const detail = document.getElementById("detailExerciseName").value;
            if (!category || !detail) {
                alert("운동 부위와 세부 운동을 모두 선택해주세요.");
                event.preventDefault();
            }
        }

        window.onload = function () {
            const detailSelect = document.getElementById("detailExerciseName");
            const calorieInput = document.querySelector("input[name='burnedCalories']");
            calorieInput.disabled = true;

            detailSelect.addEventListener("change", function () {
                const selected = this.value;
                const calories = exerciseCalories[selected];
                if (calories) {
                    calorieInput.value = calories;
                    calorieInput.disabled = false;
                } else {
                    calorieInput.value = "";
                    calorieInput.disabled = true;
                }
            });

            document.getElementById("exerciseFormSubmit").addEventListener("click", validateForm);
        };
    </script>
</head>
<body>
<div class="exercises-center">
    <div class="card exercises-card">
        <h2 class="text-center">오늘의 운동 (${today})</h2>
        <div class="text-end mb-2">
            <form id="exerciseSearchForm" method="get" action="/exercises" class="d-inline">
                <input type="date" name="searchDate" id="exerciseSearchDate" class="form-control d-inline" style="width:180px;display:inline-block;" value="${today}" required>
                <button type="submit" class="btn btn-outline-success ms-2">조회</button>
            </form>
        </div>
        <form:form method="POST" modelAttribute="exerciseForm" class="row g-2 align-items-end mb-3">
            <div class="col-md-4">
                <select id="exerciseName" name="exerciseName" class="form-control" onchange="updateDetailExercises()">
                    <option value="" disabled selected>운동 부위</option>
                    <option value="가슴">가슴</option>
                    <option value="등">등</option>
                    <option value="하체">하체</option>
                    <option value="이두">이두</option>
                    <option value="삼두">삼두</option>
                    <option value="어깨">어깨</option>
                </select>
            </div>

            <div class="col-md-4">
                <select id="detailExerciseName" name="detailExerciseName" class="form-control">
                    <option value="" disabled selected>세부 운동 선택</option>
                </select>
            </div>

            <div class="col-md-4">
                <form:input path="burnedCalories" type="number" class="form-control" placeholder="소모 칼로리"/>
            </div>

            <div class="col-md-12">
                <form:input path="date" type="date" class="form-control" value="${today}"/>
            </div>

            <div class="col-12 mt-2">
                <button id="exerciseFormSubmit" type="submit" class="btn btn-success w-100">등록</button>
            </div>
        </form:form>

        <table class="table table-striped text-center mt-4">
            <thead>
            <tr>
                <th>운동명</th>
                <th>세부 운동명</th>
                <th>소모 칼로리</th>
                <th>날짜</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ex" items="${exercises}">
                <tr>
                    <td>${ex.exerciseName}</td>
                    <td>
                        <a href="exercise/details?name=${ex.detailExerciseName}">${ex.detailExerciseName}</a>
                    </td>
                    <td>${ex.burnedCalories}</td>
                    <td>${ex.date}</td>
                    <td>
                        <a href="exercises/delete/${ex.id}" class="btn btn-danger btn-sm" onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <div class="fw-bold text-center mt-3">총 소모 칼로리: ${totalBurned} kcal</div>
        <div class="d-flex justify-content-center mt-3">
            <a href="dashboard" class="btn btn-outline-secondary">대시보드로</a>
        </div>
    </div>
</div>
</body>
</html>
