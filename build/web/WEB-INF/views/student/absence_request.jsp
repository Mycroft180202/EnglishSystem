<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Xin phép nghỉ" active="student-timetable">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Xin phép nghỉ</h3>
            <div class="text-muted small">
                <c:out value="${session.courseName}"/> · <c:out value="${session.classCode}"/>
                · <t:date value="${session.sessionDate}"/>
                · <c:out value="${session.startTime}"/>-<c:out value="${session.endTime}"/>
            </div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/timetable">Quay lại</a>
    </div>

    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/student/absence-requests/create"
          class="card"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <div class="card-body">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="sessionId" value="${sessionId}">

            <div class="mb-3">
                <label class="form-label">Lý do xin phép nghỉ</label>
                <textarea class="form-control" name="reason" rows="4" maxlength="500" required
                          placeholder="Ví dụ: Em bị ốm, xin phép nghỉ buổi học này..."></textarea>
                <div class="form-text">Yêu cầu: gửi trước buổi học ít nhất 2 tiếng. Nghỉ có phép tối đa 3 buổi.</div>
            </div>

            <button class="btn btn-primary" type="submit">Gửi đơn</button>
        </div>
    </form>
</t:layout>

