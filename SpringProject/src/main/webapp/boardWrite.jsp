<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>글쓰기</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" />
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; background: #f0f2f5; }
        .container { max-width: 720px; margin-top: 4rem; background: #fff; padding: 2rem; border-radius: 1rem; box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
        h2 { color: #43c6ac; font-weight: 800; margin-bottom: 1.5rem; text-align: center; }
        .btn-primary { background-color: #43c6ac; border: none; }
        .btn-primary:hover { background-color: #191654; }
    </style>
</head>
<body>
<div class="mb-4 text-center">
    <div style="font-size:2.5rem;font-weight:800;color:#43c6ac;letter-spacing:2px;">NutriThlet</div>
    <div style="font-size:1.1rem;color:#555;">나만의 식단과 건강을 흐름처럼 관리하다</div>
</div>
<div class="container">
    <h2>글쓰기</h2>
    <form method="post" action="write">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <div class="mb-3">
            <label class="form-label">제목</label>
            <input type="text" name="title" class="form-control" required />
        </div>
        <div class="mb-3">
            <label class="form-label">내용</label>
            <textarea name="content" class="form-control" rows="8" required></textarea>
        </div>
        <div class="d-flex justify-content-between">
            <button type="submit" class="btn btn-primary">등록</button>
            <a href="../bulletinBoard" class="btn btn-outline-secondary">목록</a>
        </div>
    </form>
</div>
</body>
</html>