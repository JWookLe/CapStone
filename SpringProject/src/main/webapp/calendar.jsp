<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>건강 관리 달력</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.css" rel="stylesheet" />
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; background-color: #f4f6f9; }
        #calendar { max-width: 900px; margin: 30px auto; background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
        .fc-daygrid-day:hover {
            cursor: pointer;
            background-color: #f1f9ff;
        }
        .fc-toolbar-title {
            font-size: 1.5rem;
            color: #343a40;
            font-weight: 600;
        }
        .fc-button {
            background-color: #0d6efd;
            border: none;
            color: white;
            border-radius: 6px;
            padding: 6px 12px;
        }
        .fc-button:hover {
            background-color: #0b5ed7;
        }
        .fc-month-select {
            padding: 4px 8px;
            border-radius: 4px;
            border: 1px solid #ccc;
            font-size: 14px;
            height: 32px;
        }
    </style>
</head>
<body>
<div class="container">
    <h2 class="text-center mt-4 mb-4 text-primary fw-bold">건강 관리 달력</h2>
    <div id="calendar"></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const calendarEl = document.getElementById('calendar');

        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            height: 600,
            customButtons: {
                goToday: {
                    text: '오늘',
                    click: function () {
                        const today = new Date();
                        const todayStr = today.toISOString().split('T')[0];
                        const fadeOut = document.body.animate([{ opacity: 1 }, { opacity: 0 }], { duration: 300 });
                        fadeOut.onfinish = function () {
                            window.location.href = "/dashboard?date=" + todayStr;
                        }
                    }
                }
            },
            headerToolbar: {
                left: 'prev,next goToday',
                center: 'title',
                right: ''
            },
            dateClick: function(info) {
                const fadeOut = document.body.animate([{ opacity: 1 }, { opacity: 0 }], { duration: 300 });
                fadeOut.onfinish = function () {
                    window.location.href = "/dashboard?date=" + info.dateStr;
                }
            },
            dayCellDidMount: function(info) {
                const dateStr = info.date.toISOString().split('T')[0];
                info.el.setAttribute('title', dateStr + ' 클릭해서 상세 보기');
            }
        });

        calendar.render();

        const monthSelect = document.createElement('select');
        monthSelect.id = 'monthSelect';
        monthSelect.className = 'fc-month-select';
        for (let i = 1; i <= 12; i++) {
            const option = document.createElement('option');
            option.value = i;
            option.text = i + '월';
            monthSelect.appendChild(option);
        }
        monthSelect.addEventListener('change', function () {
            const year = new Date().getFullYear();
            const month = this.value - 1;
            const newDate = new Date(year, month, 1);
            calendar.gotoDate(newDate);
        });

        const rightToolbar = document.querySelector('.fc-toolbar .fc-toolbar-chunk:last-child');
        if (rightToolbar) {
            rightToolbar.innerHTML = '';
            rightToolbar.appendChild(monthSelect);
        }
    });
</script>
</body>
</html>
