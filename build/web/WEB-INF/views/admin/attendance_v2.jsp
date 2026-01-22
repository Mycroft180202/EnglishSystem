<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Sửa điểm danh" active="classes">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Sửa điểm danh</h3>
            <div class="text-muted small">
                <c:out value="${session.courseName}"/> · <c:out value="${session.classCode}"/>
                · <t:date value="${session.sessionDate}"/>
                · <c:out value="${session.startTime}"/>-<c:out value="${session.endTime}"/>
                · <c:out value="${session.roomName}"/>
            </div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/classes">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/admin/attendance"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">
        <input type="hidden" name="sessionId" value="${sessionId}">

        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th>Học viên</th>
                        <th>SĐT</th>
                        <th>Thống kê</th>
                        <th>Điểm danh</th>
                        <th>Ghi chú</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${rows}" var="r">
                        <tr>
                            <td>
                                <div class="fw-semibold"><c:out value="${r.studentName}"/></div>
                                <c:if test="${not empty r.requestStatus}">
                                    <div class="small mt-1">
                                        <span class="badge text-bg-warning">Xin phép</span>
                                        <span class="text-muted ms-1"><c:out value="${r.requestCreatedAt}"/></span>
                                    </div>
                                    <div class="small text-muted"><c:out value="${r.requestReason}"/></div>
                                </c:if>
                            </td>
                            <td><c:out value="${r.studentPhone}"/></td>
                            <td class="small text-muted" style="min-width: 210px;">
                                <div>Đã học: <b><c:out value="${r.attendedCount}"/></b>/<c:out value="${r.totalSessionsToDate}"/></div>
                                <div>
                                    Vắng: <b><c:out value="${r.absentCount}"/></b> · Phép: <b><c:out value="${r.excusedCount}"/></b>/3
                                    <c:if test="${r.totalSessionsToDate > 0 && (r.absentCount + r.excusedCount) > (r.totalSessionsToDate * 0.2)}">
                                        <span class="badge text-bg-danger ms-1">Quá 20%</span>
                                    </c:if>
                                </div>
                            </td>
                            <td style="min-width: 320px;">
                                <div class="btn-group" role="group" aria-label="Attendance">
                                    <input class="btn-check" type="radio" name="status_${r.enrollId}" id="a_${r.enrollId}_att" value="ATTENDED"
                                           ${empty r.status || r.status == 'ATTENDED' ? 'checked' : ''}>
                                    <label class="btn btn-outline-success btn-sm" for="a_${r.enrollId}_att">Attended</label>

                                    <input class="btn-check" type="radio" name="status_${r.enrollId}" id="a_${r.enrollId}_abs" value="ABSENT"
                                           ${r.status == 'ABSENT' ? 'checked' : ''}>
                                    <label class="btn btn-outline-danger btn-sm" for="a_${r.enrollId}_abs">Absent</label>

                                    <input class="btn-check" type="radio" name="status_${r.enrollId}" id="a_${r.enrollId}_exc" value="EXCUSED"
                                           ${r.status == 'EXCUSED' ? 'checked' : ''}>
                                    <label class="btn btn-outline-warning btn-sm" for="a_${r.enrollId}_exc">Excused</label>
                                </div>
                            </td>
                            <td>
                                <input class="form-control" name="note_${r.enrollId}" value="${r.note}">
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr>
                            <td colspan="5" class="text-center text-muted">Chưa có học viên hoạt động trong lớp.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <button class="btn btn-primary" type="submit">Lưu điểm danh</button>
    </form>
</t:layout>
