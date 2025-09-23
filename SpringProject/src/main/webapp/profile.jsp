<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>내 정보</title>
    <link href="/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="/resources/css/health.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', sans-serif;
            background: linear-gradient(135deg, #f8fffc 0%, #e0f7fa 100%);
            min-height: 100vh;
        }
        .profile-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        .profile-header {
            width: 100%;
            max-width: 700px;
            margin: 0 auto 2rem auto;
        }
        .profile-card {
            border: none;
            border-radius: 1rem;
            box-shadow: 0 4px 24px 0 rgba(67,198,172,0.10), 0 1.5px 6px 0 rgba(67,198,172,0.10);
        }
        .nav-tabs .nav-link.active {
            background: #43c6ac;
            color: #fff;
            border: none;
            border-radius: 1rem 1rem 0 0;
        }
        .nav-tabs .nav-link {
            color: #43c6ac;
            font-weight: 600;
            border: none;
        }
        .btn-primary {
            background: #43c6ac;
            border: none;
        }
        .btn-primary:hover {
            background: #3bb49c;
        }
        .btn-danger {
            background: #f85757;
            border: none;
        }
        .btn-danger:hover {
            background: #d43c3c;
        }
        .btn-outline-secondary {
            border-color: #43c6ac;
            color: #43c6ac;
        }
        .btn-outline-secondary:hover {
            background: #43c6ac;
            color: #fff;
        }
    </style>
</head>
<body>
<div class="profile-center">
    <div class="profile-header text-center">
        <div style="font-size:2.5rem;font-weight:800;color:#43c6ac;letter-spacing:2px;">NutriThlet</div>
        <div style="font-size:1.1rem;color:#555;">나만의 식단과 건강을 흐름처럼 관리하다</div>
    </div>
    <div class="container" style="max-width:600px;">
        <ul class="nav nav-tabs justify-content-center" id="profileTab" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="info-tab" data-bs-toggle="tab" data-bs-target="#info" type="button" role="tab">내 정보 확인</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="pw-tab" data-bs-toggle="tab" data-bs-target="#pw" type="button" role="tab">비밀번호 변경</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="delete-tab" data-bs-toggle="tab" data-bs-target="#delete" type="button" role="tab">회원 탈퇴</button>
            </li>
        </ul>
        <div class="tab-content mt-4" id="profileTabContent">
            <!-- 내 정보 확인 -->
            <div class="tab-pane fade show active" id="info" role="tabpanel">
                <div class="card profile-card shadow-sm mb-4">
                    <div class="card-body">
                        <table class="table mb-0">
                            <tr><th class="bg-light text-end">이름</th><td>${user.username}</td></tr>
                        </table>
                    </div>
                </div>
            </div>
            <!-- 비밀번호 변경 -->
            <div class="tab-pane fade" id="pw" role="tabpanel">
                <div class="card profile-card shadow-sm mb-4">
                    <div class="card-body">
                        <form method="post" action="/profile/password" id="pwForm" onsubmit="return validatePwForm();">
                            <c:if test='${not empty error}'>
                                <div id="pwErrorMsg" class="alert alert-danger text-center mb-2" style="display:block;">
                                    <c:out value="${error}" />
                                    <c:if test="${error eq '현재 비밀번호가 일치하지 않습니다.'}">
                                        <br>비밀번호 입력란이 모두 초기화되었습니다. 다시 입력해 주세요.
                                    </c:if>
                                </div>
                            </c:if>
                            <c:if test='${empty error}'>
                                <div id="pwErrorMsg" class="alert alert-danger text-center mb-2" style="display:none;"></div>
                            </c:if>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div class="mb-3 row">
                                <label for="currentPassword" class="col-sm-4 col-form-label text-end">현재 비밀번호</label>
                                <div class="col-sm-8">
                                    <input type="password" class="form-control" id="currentPassword" name="currentPassword" required>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="newPassword" class="col-sm-4 col-form-label text-end">새 비밀번호</label>
                                <div class="col-sm-8">
                                    <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                                </div>
                            </div>
                            <div class="mb-3 row">
                                <label for="confirmPassword" class="col-sm-4 col-form-label text-end">새 비밀번호 확인</label>
                                <div class="col-sm-8">
                                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                                </div>
                            </div>
                            <div class="text-center">
                                <button type="submit" class="btn btn-primary px-4">비밀번호 변경</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <!-- 회원 탈퇴 -->
            <div class="tab-pane fade" id="delete" role="tabpanel">
                <div class="card profile-card shadow-sm mb-4">
                    <div class="card-body text-center">
                        <form method="post" action="/profile/delete" id="deleteForm" onsubmit="return validateDeleteForm();">
                            <div id="deleteErrorMsg" class="alert alert-danger text-center mb-2" style="display:none;"></div>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div class="mb-3 text-start">
                                <label class="form-label fw-bold">탈퇴 사유를 선택해 주세요</label>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason1" value="서비스 불만족" required>
                                    <label class="form-check-label" for="reason1">서비스 불만족</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason2" value="사용이 불편함">
                                    <label class="form-check-label" for="reason2">사용이 불편함</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason3" value="개인정보 우려">
                                    <label class="form-check-label" for="reason3">개인정보 우려</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason4" value="원하는 기능 부족">
                                    <label class="form-check-label" for="reason4">원하는 기능 부족</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason5" value="이용 빈도 낮음">
                                    <label class="form-check-label" for="reason5">이용 빈도 낮음</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason6" value="다른 서비스 이용">
                                    <label class="form-check-label" for="reason6">다른 서비스 이용</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="reason" id="reason7" value="기타">
                                    <label class="form-check-label" for="reason7">기타</label>
                                </div>
                            </div>
                            <button type="submit" class="btn btn-danger px-4">회원 탈퇴</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
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
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
function validatePwForm() {
    var cur = document.getElementById('currentPassword').value.trim();
    var pw = document.getElementById('newPassword').value.trim();
    var pw2 = document.getElementById('confirmPassword').value.trim();
    var msg = '';
    if (!cur || !pw || !pw2) {
        msg = '모든 정보를 입력하세요.';
    } else if (pw.length < 8) {
        msg = '새 비밀번호는 8자리 이상이어야 합니다.';
    } else if (pw !== pw2) {
        msg = '새 비밀번호와 확인이 일치하지 않습니다.';
    }
    if (msg) {
        document.getElementById('pwErrorMsg').innerText = msg;
        document.getElementById('pwErrorMsg').style.display = '';
        return false;
    }
    document.getElementById('pwErrorMsg').style.display = 'none';
    return true;
}
function validateDeleteForm() {
    var checked = document.querySelector('#deleteForm input[name="reason"]:checked');
    if (!checked) {
        document.getElementById('deleteErrorMsg').innerText = '탈퇴 사유를 선택해 주세요.';
        document.getElementById('deleteErrorMsg').style.display = '';
        return false;
    }
    document.getElementById('deleteErrorMsg').style.display = 'none';
    return true;
}
document.addEventListener('DOMContentLoaded', function() {
    var errorMsg = "${error}";
    if (errorMsg && errorMsg.length > 0) {
        var pwTab = document.getElementById('pw-tab');
        if (pwTab) pwTab.click();
    }
});
</script>
</body>
</html> 