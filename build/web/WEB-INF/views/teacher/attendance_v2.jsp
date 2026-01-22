<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Điểm danh" active="teacher-sessions">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Điểm danh</h3>
            <div class="text-muted small">
                <c:out value="${session.courseName}"/> · <c:out value="${session.classCode}"/>
                · <t:date value="${session.sessionDate}"/>
                · <c:out value="${session.startTime}"/>-<c:out value="${session.endTime}"/>
                · <c:out value="${session.roomName}"/>
            </div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/teacher/sessions">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/teacher/attendance"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">
        <input type="hidden" name="sessionId" value="${sessionId}">

        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th>Học viên</th>
                        <th>SĐT</th>
                        <th>Trạng thái</th>
                        <th>Ghi chú</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${rows}" var="r">
                        <tr>
                            <td><c:out value="${r.studentName}"/></td>
                            <td><c:out value="${r.studentPhone}"/></td>
                            <td style="min-width: 220px;">
                                <select class="form-select" name="status_${r.enrollId}">
                                    <option value="PRESENT" ${empty r.status || r.status == 'PRESENT' ? 'selected' : ''}>PRESENT</option>
                                    <option value="ABSENT" ${r.status == 'ABSENT' ? 'selected' : ''}>ABSENT</option>
                                    <option value="LATE" ${r.status == 'LATE' ? 'selected' : ''}>LATE</option>
                                    <option value="EXCUSED" ${r.status == 'EXCUSED' ? 'selected' : ''}>EXCUSED</option>
                                </select>
                            </td>
                            <td>
                                <input class="form-control" name="note_${r.enrollId}" value="${r.note}">
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr>
                            <td colspan="4" class="text-center text-muted">Chưa có học viên hoạt động trong lớp.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <button class="btn btn-primary" type="submit">Lưu điểm danh</button>
    </form>
</t:layout>

