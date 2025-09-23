<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>케이블 컬</title>
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
    <div class="card-header">케이블 컬</div>
    <p><strong>운동 설명:</strong> 케이블 컬(Cable Curl)은 케이블 머신을 이용해 일정한 장력을 유지하며 이두근을 자극하는 운동으로, 전통적인 바벨/덤벨 컬보다 운동 내내 근육의 긴장이 유지되어 더 지속적이고 집중적인 이두근 자극이 가능합니다.</p>
    <p><strong>자극 부위:</strong> 이두근</p>

    <div class="d-flex justify-content-between mt-4">
        <a href="/exercises" class="btn btn-outline-secondary">이전으로</a>
        <a href="https://www.youtube.com/watch?v=acLw_29-8C0" target="_blank" class="btn btn-danger">유튜브 영상 보기</a>
    </div>
</div>
</body>
</html>
