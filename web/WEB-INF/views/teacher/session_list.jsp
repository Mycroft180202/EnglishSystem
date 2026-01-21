<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Buổi dạy" active="teacher-sessions">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Buổi dạy của tôi</h3>
        <div class="text-muted small">
            Nhập điểm theo lớp: chọn 1 lớp trong mục Lớp học (Admin gán đúng giáo viên).
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Ngày</th>
                    <th>Giờ</th>
                    <th>Phòng</th>
                    <th>Ca</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Điểm danh</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${sessions}" var="s">
                    <tr>
                        <td><t:date value="${s.sessionDate}"/></td>
                        <td><c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/></td>
                        <td><c:out value="${s.roomName}"/></td>
                        <td><c:out value="${s.slotName}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${s.status}"/></span></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/teacher/attendance?sessionId=${s.sessionId}">Điểm danh</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty sessions}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có buổi học. Admin cần Generate từ lịch.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
