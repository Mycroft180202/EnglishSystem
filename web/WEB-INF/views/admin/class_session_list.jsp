<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Buổi học" active="class-sessions">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Buổi học (class_sessions)</h3>
            <div class="text-muted">
                <c:out value="${clazz.className}"/> - <c:out value="${clazz.courseName}"/> (<c:out value="${clazz.classCode}"/>)
            </div>
        </div>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/class-schedules?classId=${clazz.classId}">Lịch học</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/classes">Quay lại</a>
        </div>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Ngày</th>
                    <th>Ca</th>
                    <th>Giờ</th>
                    <th>Phòng</th>
                    <th>Giáo viên</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${sessions}" var="s">
                    <tr>
                        <td><t:date value="${s.sessionDate}"/></td>
                        <td><c:out value="${s.slotName}"/></td>
                        <td><c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/></td>
                        <td><c:out value="${s.roomName}"/></td>
                        <td><c:out value="${s.teacherName}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${s.status}"/></span></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty sessions}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có buổi học. Hãy Generate từ Lịch học.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
