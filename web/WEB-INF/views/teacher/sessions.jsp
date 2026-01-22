<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Buổi dạy" active="teacher-sessions">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-2">
        <div>
            <h3 class="m-0">Buổi dạy của tôi</h3>
            <div class="text-muted small">
                Điểm danh chỉ sửa được trong ngày. Nếu muốn sửa điểm danh ngày khác, Admin sẽ thực hiện.
            </div>
        </div>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="card mb-3">
        <div class="card-body">
            <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-2">
                <h5 class="m-0">Hôm nay (<t:date value="${today}"/>)</h5>
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/teacher/sessions">Về tuần hiện tại</a>
            </div>
            <div class="table-responsive">
                <table class="table table-striped table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Lớp</th>
                            <th>Giờ</th>
                            <th>Phòng</th>
                            <th>Ca</th>
                            <th>Trạng thái</th>
                            <th class="text-end">Điểm danh</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${todaySessions}" var="s">
                            <tr>
                                <td>
                                    <div class="fw-semibold"><c:out value="${empty s.classCode ? s.className : s.classCode}"/></div>
                                    <div class="small text-muted"><c:out value="${s.courseName}"/></div>
                                </td>
                                <td><c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/></td>
                                <td><c:out value="${s.roomName}"/></td>
                                <td><c:out value="${s.slotName}"/></td>
                                <td><span class="badge text-bg-secondary"><c:out value="${s.status}"/></span></td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/teacher/attendance?sessionId=${s.sessionId}">Điểm danh</a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty todaySessions}">
                            <tr>
                                <td colspan="6" class="text-center text-muted">Hôm nay không có buổi dạy.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-2">
        <div>
            <h5 class="m-0">Thời khóa biểu tuần</h5>
            <div class="text-muted small">Bấm vào ô để xem chi tiết. Điểm danh chỉ bật khi đúng ngày.</div>
        </div>
    </div>

    <%@include file="/WEB-INF/views/_timetable.jspf"%>
</t:layout>
