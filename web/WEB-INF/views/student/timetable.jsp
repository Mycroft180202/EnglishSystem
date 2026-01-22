<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Thời khóa biểu tuần" active="student-timetable">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-2">
        <div>
            <h3 class="m-0">Thời khóa biểu tuần</h3>
            <div class="text-muted">Xem lịch học theo tuần (tự động theo lớp bạn đang đăng ký)</div>
        </div>
    </div>

    <%@include file="/WEB-INF/views/_timetable.jspf"%>

    <div class="d-flex flex-wrap gap-3 align-items-center small text-muted mt-2">
        <div class="d-flex align-items-center gap-2">
            <span class="dot dot-upcoming"></span> Chưa học
        </div>
        <div class="d-flex align-items-center gap-2">
            <span class="dot dot-done"></span> Attended (Có mặt)
        </div>
        <div class="d-flex align-items-center gap-2">
            <span class="dot dot-absent"></span> Absent (Vắng mặt)
        </div>
        <div class="d-flex align-items-center gap-2">
            <span class="dot dot-excused"></span> Excused (Nghỉ phép)
        </div>
    </div>
</t:layout>
