<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--@elvariable id="userForm" type="com.example.springproject.model.User"--%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>


<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>회원가입</title>
    <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="${contextPath}/resources/css/health.css" rel="stylesheet">
    <style>
        body { font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif; }
        .register-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .register-card {
            width: 100%;
            max-width: 500px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem 2.5rem 2rem 2.5rem;
        }
        .register-card h2 {
            font-size: 2.2rem;
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
        .btn-link {
            font-size: 1.1rem;
            border-radius: 1.5rem;
        }
        .text-center span {
            font-size: 1.1rem;
        }
    </style>
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet">
</head>

<body>

<div class="register-center">
    <div class="mb-4 text-center">
        <div style="font-size:2.5rem;font-weight:800;color:#43c6ac;letter-spacing:2px;">NutriThlet</div>
        <div style="font-size:1.1rem;color:#555;">나만의 식단과 건강을 흐름처럼 관리하다</div>
    </div>
    <div class="card register-card p-4">
        <h2 class="text-center mb-4">회원가입</h2>
        <form:form method="POST" modelAttribute="userForm" id="registerForm">

            <spring:bind path="username">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <form:input type="text" path="username" class="form-control" placeholder="이름"/>
                    <form:errors path="username" cssClass="text-danger small"/>
                </div>
            </spring:bind>

            <spring:bind path="id">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <form:input type="text" path="id" class="form-control" placeholder="ID" autofocus="true"/>
                    <form:errors path="id" cssClass="text-danger small"/>
                </div>
            </spring:bind>

            <spring:bind path="password">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <form:input type="password" path="password" class="form-control" placeholder="비밀번호"/>
                    <form:errors path="password" cssClass="text-danger small"/>
                </div>
            </spring:bind>
            <spring:bind path="passwordConfirm">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <form:input type="password" path="passwordConfirm" class="form-control" placeholder="비밀번호 확인"/>
                    <form:errors path="passwordConfirm" cssClass="text-danger small"/>
                </div>
            </spring:bind>
            <spring:bind path="height">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <form:input type="number" step="0.1" path="height" class="form-control" placeholder="키 (cm, 소수점 1자리)" min="0" max="300" id="heightInput"/>
                    <form:errors path="height" cssClass="text-danger small"/>
                </div>
            </spring:bind>

            <spring:bind path="gender">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <label>성별</label><br/>
                    <form:radiobutton path="gender" value="M" id="genderMale"/>
                    <label for="genderMale">남자</label>
                    <form:radiobutton path="gender" value="F" id="genderFemale" style="margin-left:20px;"/>
                    <label for="genderFemale">여자</label>
                    <form:errors path="gender" cssClass="text-danger small"/>
                </div>
            </spring:bind>
            <spring:bind path="birthDate">
                <div class="mb-3 ${status.error ? 'has-error' : ''}">
                    <label>나이</label>
                    <form:input type="date" path="birthDate" class="form-control" placeholder="생년월일"/>
                    <form:errors path="birthDate" cssClass="text-danger small"/>
                </div>
            </spring:bind>
            <button class="btn btn-primary w-100 mb-2" type="submit">회원가입</button>
        </form:form>
        <div class="text-center mt-2">
            <span>이미 계정이 있으신가요?</span>
            <a href="${contextPath}/login" class="btn btn-link">로그인</a>
        </div>
    </div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>

<script>
    // 입력 시 소수점 1자리까지만 입력 자체가 불가능하게 막기
    document.getElementById('heightInput').addEventListener('input', function(e) {
        let value = e.target.value;
        // 정규식: 정수 1~3자리 또는 소수점 1자리까지만 허용
        const valid = /^\d{0,3}(\.\d{0,1})?$/.test(value);
        if (!valid) {
            // 마지막 입력을 무시 (이전 값으로 복원)
            e.target.value = value.slice(0, -1);
        }
    });

    document.getElementById('registerForm').addEventListener('submit', function(e) {
        const form = this;
        const username = form.username.value.trim();
        const id = form.id.value.trim();
        const password = form.password.value.trim();
        const passwordConfirm = form.passwordConfirm.value.trim();
        const height = form.height.value.trim();
        const genderM = form.querySelector('#genderMale').checked;
        const genderF = form.querySelector('#genderFemale').checked;
        const birthDate = form.birthDate.value.trim();

        if (!username || !id || !password || !passwordConfirm || !height || !(genderM || genderF) || !birthDate) {
            alert('모든 정보를 입력해주세요.');
            e.preventDefault();
            return;
        }
        // height 소수점 1자리까지만 허용
        if (!/^\d{1,3}(\.\d)?$/.test(height)) {
            alert('키는 소수점 1자리까지만 입력 가능합니다. 예: 170.5');
            e.preventDefault();
            return;
        }
    });
</script>

</body>
</html>
