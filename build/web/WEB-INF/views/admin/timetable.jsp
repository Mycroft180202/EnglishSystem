<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Thời khóa biểu tuần" active="timetable">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-2">
        <div>
            <h3 class="m-0">Thời khóa biểu tuần</h3>
            <div class="text-muted">
                <c:out value="${clazz.className}"/> - <c:out value="${clazz.courseName}"/> (<c:out value="${clazz.classCode}"/>)
                · GV: <c:out value="${clazz.teacherName}"/>
                · Phòng: <c:out value="${clazz.roomName}"/>
            </div>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/class-schedules?classId=${clazz.classId}">Lịch học</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/class-sessions?classId=${clazz.classId}">Buổi học</a>
        </div>
    </div>

    <%@include file="/WEB-INF/views/_timetable.jspf"%>
</t:layout>
