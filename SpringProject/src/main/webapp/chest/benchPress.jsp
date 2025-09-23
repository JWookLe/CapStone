<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>벤치프레스</title>
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
    <div class="card-header">벤치프레스</div>
    <p><strong>운동 설명:</strong> 벤치프레스는 상체 근육을 중심으로 가슴, 어깨, 팔 근육을 함께 단련하는 대표적인 웨이트 트레이닝 운동입니다. 주로 바벨을 사용하며, 올바른 자세로 수행하면 가슴 근육을 효율적으로 강화할 수 있습니다.</p>
    <p><strong>자극 부위:</strong> 대흉근(가슴), 삼두근(팔 뒤쪽), 전면 삼각근(어깨 앞부분)</p>

    <div class="d-flex justify-content-between mt-4">
        <a href="/exercises" class="btn btn-outline-secondary">이전으로</a>
        <a href="https://youtu.be/rDA8UXczGfg" target="_blank" class="btn btn-danger">
            유튜브 영상 보기
        </a>
    </div>
</div>
</body>
</html>
