<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>게시판</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" />
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; background: #eef2f7; }
        .container { max-width: 900px; margin-top: 4rem; background: #fff; padding: 2rem; border-radius: 1rem; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
        h2 { color: #43c6ac; font-weight: bold; margin-bottom: 2rem; text-align: center; }
        .btn-primary { background-color: #43c6ac; border: none; margin-bottom: 1rem; }
        .btn-primary:hover { background-color: #191654; }
        table th {
            background-color: #f8f9fa;
            color: #333;
            text-align: center;
            font-size: 1.05rem;
            padding: 0.9rem;
            border-bottom: 2px solid #dee2e6;
        }
        table td { text-align: center; vertical-align: middle; }
        a { text-decoration: none; color: #43c6ac; }
        a:hover { color: #191654; text-decoration: underline; }
        .dashboard-link {
            margin-top: 2rem;
            display: flex;
            justify-content: center;
        }
    </style>
</head>
<body>
<div class="mb-4 text-center">
    <div style="font-size:2.5rem;font-weight:800;color:#43c6ac;letter-spacing:2px;">NutriThlet</div>
    <div style="font-size:1.1rem;color:#555;">나만의 식단과 건강을 흐름처럼 관리하다</div>
</div>
<div class="container">
    <h2>게시판</h2>
    <a href="bulletinBoard/write" class="btn btn-primary">글쓰기</a>
    <table class="table table-hover">
        <thead>
        <tr>
            <th>번호</th>
            <th>제목</th>
            <th>작성자</th>
            <th>작성일</th>
            <th>조회수</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="post" items="${postList}">
            <tr>
                <td>${post.id}</td>
                <td><a href="bulletinBoard/view?id=${post.id}">${post.title}</a></td>
                <td>${post.maskedUserId}</td>
                <td>${post.createdAt}</td>
                <td>${post.viewCount}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
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
