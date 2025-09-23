<%@ page contentType="text/html;charset=UTF-8" %>
<%--@elvariable id="weightForm" type="com.example.springproject.model.Weight"--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<head>
    <title>몸무게 기록</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        /* 폰트 및 전체 배경 */
        body {
            font-family: 'Noto Sans KR', 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
            background: linear-gradient(135deg, #e0f7fa 0%, #ffffff 100%);
            color: #333;
            line-height: 1.6;
            margin: 0;
            padding: 0;
        }

        /* 전체 레이아웃 */
        .report-center {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
            align-items: center;
            padding: 3rem 1rem 5rem;
            background: #f7fbfc;
        }

        /* 카드 스타일 */
        .report-card {
            width: 100%;
            max-width: 720px;
            background: #ffffff;
            border-radius: 2rem;
            box-shadow: 0 12px 30px rgba(0,0,0,0.12);
            padding: 3rem 3rem 4rem;
            margin-bottom: 3rem;
            transition: box-shadow 0.3s ease;
        }

        .report-card:hover {
            box-shadow: 0 20px 45px rgba(0,0,0,0.18);
        }

        /* 제목 */
        h2 {
            font-size: 2.8rem;
            font-weight: 900;
            color: #43c6ac;
            letter-spacing: 3px;
            margin-bottom: 1.5rem;
            text-align: center;
            user-select: none;
        }

        h3 {
            font-size: 2rem;
            font-weight: 800;
            color: #238636;
            border-left: 6px solid #43c6ac;
            padding-left: 1rem;
            margin-bottom: 1rem;
            user-select: none;
        }

        /* 폼 스타일 */
        form {
            display: flex;
            flex-wrap: wrap;
            gap: 1.2rem 2rem;
            align-items: center;
            justify-content: center;
            margin-bottom: 2rem;
        }

        form label {
            font-weight: 600;
            font-size: 1.1rem;
            color: #444;
            width: 80px;
            user-select: none;
        }

        form input[type="date"],
        form input[type="number"] {
            padding: 0.5rem 1rem;
            font-size: 1.1rem;
            border: 2px solid #43c6ac;
            border-radius: 1.5rem;
            outline: none;
            width: 180px;
            transition: border-color 0.3s ease;
        }

        form input[type="date"]:focus,
        form input[type="number"]:focus {
            border-color: #191654;
        }

        form button {
            background: #43c6ac;
            color: white;
            border: none;
            padding: 0.8rem 2.5rem;
            font-size: 1.2rem;
            border-radius: 2rem;
            cursor: pointer;
            font-weight: 700;
            transition: background-color 0.3s ease;
            user-select: none;
        }

        form button:hover {
            background-color: #191654;
            box-shadow: 0 4px 14px rgba(25,22,84,0.7);
        }

        /* 테이블 스타일 */
        table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
            border-radius: 1.5rem;
            overflow: hidden;
            box-shadow: inset 0 0 8px rgba(0,0,0,0.05);
            background-color: #f8f9fa;
            font-size: 1.1rem;
        }

        thead tr {
            background: linear-gradient(90deg, #43c6ac 0%, #191654 100%);
            color: white;
            font-weight: 700;
        }

        thead th {
            padding: 1rem 1.2rem;
            user-select: none;
        }

        tbody tr {
            transition: background-color 0.3s ease;
        }

        tbody tr:hover {
            background-color: #d7f3f1;
            cursor: default;
        }

        tbody td {
            padding: 1rem 1.2rem;
            text-align: center;
            color: #444;
        }

        /* 링크 스타일 (수정) */
        a {
            color: #43c6ac;
            font-weight: 600;
            text-decoration: none;
            user-select: none;
            transition: color 0.3s ease;
        }

        a:hover {
            color: #191654;
            text-decoration: underline;
        }

        /* 삭제 버튼 */
        form.delete-form button {
            background: transparent;
            border: 2px solid #ff4d4d;
            color: #ff4d4d;
            padding: 0.4rem 1.2rem;
            border-radius: 2rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            user-select: none;
        }

        form.delete-form button:hover {
            background-color: #ff4d4d;
            color: white;
            box-shadow: 0 4px 14px rgba(255,77,77,0.7);
        }

        /* 그래프 캔버스 */
        #weightChart {
            width: 100% !important;
            height: 300px !important;
            user-select: none;
        }

        /* 반응형 */
        @media (max-width: 768px) {
            .report-card {
                padding: 2rem 2rem 3rem;
                max-width: 95%;
            }
            h2 {
                font-size: 2rem;
                margin-bottom: 1rem;
            }
            h3 {
                font-size: 1.5rem;
            }
            form label {
                width: 100%;
                margin-bottom: 0.3rem;
                font-size: 1rem;
            }
            form input[type="date"],
            form input[type="number"] {
                width: 100%;
            }
            form {
                gap: 1rem;
                justify-content: stretch;
            }
            form button {
                width: 100%;
                padding: 0.8rem;
                font-size: 1.1rem;
            }
            table {
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>
<div class="report-center">

    <div class="report-card">
        <h2>${editWeight.id != null ? "몸무게 수정" : "몸무게 등록"}</h2>

        <form:form modelAttribute="weightForm" method="post" action="/weightChange/save">
            <form:hidden path="id"/>
            <form:hidden path="userId"/>

            <label for="date">날짜</label>
            <form:input path="date" id="date" type="date" required="true"/>

            <label for="weight">몸무게 (kg)</label>
            <form:input path="weight" id="weight" type="number" step="0.1" required="true"/>

            <button type="submit">
                    ${editWeight.id != null ? "수정" : "등록"}
            </button>
        </form:form>
        <c:if test="${not empty msg}">
            <script>
                alert("${msg}");
            </script>
        </c:if>
    </div>

    <div class="report-card">
        <h3>몸무게 기록</h3>
        <table>
            <thead>
            <tr>
                <th>날짜</th>
                <th>몸무게</th>
                <th>삭제</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="w" items="${weights}">
                <tr>
                    <td>${w.date}</td>
                    <td>${w.weight} kg</td>
                    <td>
                        <form action="${pageContext.request.contextPath}/weightChange/delete" method="post" class="delete-form" style="display:inline;">
                            <input type="hidden" name="id" value="${w.id}" />
                            <input type="hidden" name="userId" value="${userId}" />
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" onclick="return confirm('삭제하시겠습니까?');">삭제</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="report-card">
        <h3>몸무게 변화 그래프</h3>
        <canvas id="weightChart"></canvas>
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

<script>
    // 날짜 라벨 배열 생성
    const labels = [
        <c:forEach var="w" items="${weights}" varStatus="status">
        "${w.date}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // 몸무게 데이터 배열 생성
    const data = [
        <c:forEach var="w" items="${weights}" varStatus="status">
        ${w.weight}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    // 최소/최대값 계산
    const minWeight = Math.min(...data);
    const maxWeight = Math.max(...data);

    // y축 범위에 약간 여유 추가 (1kg)
    const yMin = minWeight - 1 > 0 ? minWeight - 1 : 0;
    const yMax = maxWeight + 1;

    const ctx = document.getElementById('weightChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '몸무게 (kg)',
                data: data,
                fill: false,
                borderColor: '#43c6ac',
                backgroundColor: '#43c6ac',
                tension: 0.15,
                pointRadius: 5,
                pointHoverRadius: 7,
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: {
                        font: {
                            size: 14,
                            weight: '700'
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    min: yMin,
                    max: yMax,
                    ticks: {
                        font: {
                            size: 13,
                            weight: '600'
                        }
                    }
                },
                x: {
                    ticks: {
                        font: {
                            size: 13,
                            weight: '600'
                        }
                    }
                }
            }
        }
    });
</script>

</body>
</html>
