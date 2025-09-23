<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>펙덱 플라이</title>
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
    <div class="card-header">펙덱 플라이</div>
    <p><strong>운동 설명:</strong> 펙덱 플라이는 머신을 이용해 팔을 벌리고 모으는 동작을 통해 가슴 근육을 고립시켜 자극하는 운동입니다. 초보자에게도 안정적으로 수행 가능합니다.</p>
    <p><strong>자극 부위:</strong> 대흉근 (중앙), 소흉근</p>

    <div class="d-flex justify-content-between mt-4">
        <a href="/exercises" class="btn btn-outline-secondary">이전으로</a>
        <a href="https://www.youtube.com/watch?v=bo6ZlZ_qL48" target="_blank" class="btn btn-danger">
            유튜브 영상 보기
        </a>
    </div>
</div>
</body>
</html>