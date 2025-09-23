<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>데드리프트</title>
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
    <div class="card-header">데드리프트</div>
    <p><strong>운동 설명:</strong> 데드리프트는 하체와 등 전체를 활용해 바벨을 바닥에서 들어올리는 대표적인 전신 복합 운동입니다.</p>
    <p><strong>자극 부위:</strong> 척추기립근, 햄스트링, 둔근, 광배근</p>

    <div class="d-flex justify-content-between mt-4">
        <a href="/exercises" class="btn btn-outline-secondary">이전으로</a>
        <a href="https://www.youtube.com/watch?v=hRVNKm9K4zU" target="_blank" class="btn btn-danger">유튜브 영상 보기</a>
    </div>
</div>
</body>
</html>