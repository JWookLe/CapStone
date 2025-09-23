<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>카프 레이즈</title>
    <link href="/resources/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #f8ffae 0%, #43c6ac 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            font-family: 'Noto Sans KR', sans-serif;
        }
        .card {
            border-radius: 1rem;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
            padding: 2rem;
            max-width: 600px;
            width: 100%;
            background-color: #fff;
        }
        .card-header {
            font-size: 1.5rem;
            font-weight: bold;
            text-align: center;
            margin-bottom: 1.5rem;
        }
        .btn {
            border-radius: 2rem;
            font-size: 1.1rem;
            padding: 0.6rem 1.5rem;
        }
    </style>
</head>
<body>
<div class="card">
    <div class="card-header">카프 레이즈</div>
    <p><strong>운동 설명:</strong> 카프 레이즈는 발끝을 이용해 몸을 위로 들어올리는 동작으로, 종아리 근육(비복근과 가자미근)을 강화하는 대표적인 하체 보조 운동입니다.</p>
    <p><strong>자극 부위:</strong> 비복근, 가자미근 (종아리)</p>
    <div class="d-flex justify-content-between mt-4">
        <a href="/exercises" class="btn btn-outline-secondary">이전으로</a>
        <a href="https://www.youtube.com/watch?v=zI0_oDgVB9s" target="_blank" class="btn btn-danger">유튜브 영상 보기</a>
    </div>
</div>
</body>
</html>