<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>로그인</title>
    <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="${contextPath}/resources/css/health.css" rel="stylesheet">
    <style>
        body { font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif; }
        .login-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .login-card {
            width: 100%;
            max-width: 480px;
            border-radius: 2.5rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            padding: 2.5rem 2.5rem 2rem 2.5rem;
        }
        .login-card h2 {
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
<div class="login-center">
    <div class="mb-4 text-center">
        <div style="font-size:2.5rem;font-weight:800;color:#43c6ac;letter-spacing:2px;">NutriThlet</div>
        <div style="font-size:1.1rem;color:#555;">나만의 식단과 건강을 흐름처럼 관리하다</div>
    </div>
    <div class="card login-card p-4">
        <h2 class="text-center mb-4">로그인</h2>
        <c:if test="${passwordChanged}">
            <div class="alert alert-success text-center mb-3">비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해 주세요.</div>
        </c:if>
        <c:if test="${deleted}">
            <div class="alert alert-info text-center mb-3">그동안 이용해주셔서 감사합니다.</div>
        </c:if>
        <form method="POST" action="${contextPath}/login">
            <div class="mb-3">
                <input name="id" type="text" class="form-control" placeholder="ID" autofocus/>
            </div>
            <div class="mb-3">
                <input name="password" type="password" class="form-control" placeholder="Password"/>
            </div>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center mb-2">${error}</div>
            </c:if>
            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-danger">${sessionScope.errorMessage}</div>
            </c:if>
            <button class="btn btn-primary w-100 mb-2" type="submit">로그인</button>
        </form>
        <div class="text-center mt-2">
            <span>계정이 없으신가요?</span>
            <a href="${contextPath}/registration" class="btn btn-link">회원가입</a>
        </div>
    </div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>