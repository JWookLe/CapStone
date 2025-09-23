<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>게시글 상세</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" />
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; background: #f9f9f9; }
        .container { max-width: 800px; margin-top: 4rem; background: #fff; padding: 2.5rem; border-radius: 1.2rem; box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
        h2 { color: #43c6ac; font-weight: bold; }
        .metadata { font-size: 0.9rem; color: #666; margin-bottom: 1rem; }
        textarea, input[type="text"] { border-radius: 1rem; padding: 0.5rem 1rem; border: 1px solid #ccc; width: 100%; }
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
    <h2>${post.title}</h2>
    <div class="metadata">작성자: ${post.maskedUserId} | 작성일: ${post.createdAt} | 조회수: ${post.viewCount}</div>
    <div class="mb-4">${post.content}</div>
    <a href="../bulletinBoard" class="btn btn-outline-secondary mb-3">목록</a>
    <c:if test="${post.userId eq pageContext.request.userPrincipal.name or pageContext.request.userPrincipal.name == 'administer1234'}">
    <form method="post" action="delete" style="display:inline;">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="id" value="${post.id}" />
            <button type="submit" class="btn btn-danger mb-3" onclick="return confirm('정말 삭제하시겠습니까?');">삭제</button>
        </form>
    </c:if>
    <button type="button" class="btn btn-warning mb-3" data-bs-toggle="modal" data-bs-target="#reportModal">신고</button>

    <!-- 신고 모달 -->
    <div class="modal fade" id="reportModal" tabindex="-1" aria-labelledby="reportModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <form method="post" action="report">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="postId" value="${post.id}" />
            <div class="modal-header">
              <h5 class="modal-title" id="reportModalLabel">게시글 신고</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <div>신고 사유를 선택해주세요:</div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason1" value="음란/선정적" required>
                <label class="form-check-label" for="reason1">음란/선정적</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason2" value="폭력/혐오">
                <label class="form-check-label" for="reason2">폭력/혐오</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason3" value="광고/도배">
                <label class="form-check-label" for="reason3">광고/도배</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason4" value="개인정보노출">
                <label class="form-check-label" for="reason4">개인정보노출</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason5" value="저작권침해">
                <label class="form-check-label" for="reason5">저작권침해</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason6" value="욕설/비방">
                <label class="form-check-label" for="reason6">욕설/비방</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="reason" id="reason7" value="기타">
                <label class="form-check-label" for="reason7">기타</label>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
              <button type="submit" class="btn btn-warning">신고하기</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <hr>
    <h5>댓글</h5>
    <form method="post" action="comment">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input type="hidden" name="postId" value="${post.id}" />
        <textarea name="content" rows="2" class="form-control mb-2" placeholder="댓글을 입력하세요" required></textarea>
        <button type="submit" class="btn btn-primary btn-sm">댓글 등록</button>
    </form>
    <div class="mt-3">
        <c:forEach var="comment" items="${commentList}">
            <c:set var="marginLeft" value="${comment.depth * 30}" scope="page" />
            <div style="margin-left: ${pageScope.marginLeft}px; margin-top: 1rem;">
                <b>${comment.maskedUserId}</b> <small class="text-muted">(${comment.createdAt})</small><br>
                    ${comment.content}
                <form method="post" action="comment" class="mt-1">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <input type="hidden" name="postId" value="${post.id}" />
                    <input type="hidden" name="parentId" value="${comment.id}" />
                    <input type="text" name="content" placeholder="대댓글" required class="form-control mt-1 mb-1" />
                    <button type="submit" class="btn btn-link btn-sm">대댓글</button>
                </form>
            </div>
        </c:forEach>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>