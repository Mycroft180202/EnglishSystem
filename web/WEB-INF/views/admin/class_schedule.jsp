<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Lịch học lớp" active="class-schedules">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Lịch học lớp</h3>
            <div class="text-muted">
                <c:out value="${clazz.className}"/> - <c:out value="${clazz.courseName}"/> (<c:out value="${clazz.classCode}"/>)
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

    <div class="card mb-3">
        <div class="card-body">
            <h5 class="card-title mb-3">Thêm lịch học</h5>
            <form method="post" action="${pageContext.request.contextPath}/admin/class-schedules"
                  class="row g-3"
                  onsubmit="this.querySelector('button[type=submit]').disabled=true;">
                <input type="hidden" name="formToken" value="${formToken}">
                <input type="hidden" name="classId" value="${clazz.classId}">

                <div class="col-md-3">
                    <label class="form-label">Thứ</label>
                    <select class="form-select" name="dayOfWeek" required>
                        <option value="1">Thứ 2</option>
                        <option value="2">Thứ 3</option>
                        <option value="3">Thứ 4</option>
                        <option value="4">Thứ 5</option>
                        <option value="5">Thứ 6</option>
                        <option value="6">Thứ 7</option>
                        <option value="7">Chủ nhật</option>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label">Ca học</label>
                    <select class="form-select" name="slotId" required>
                        <option value="">-- Chọn ca --</option>
                        <c:forEach items="${slots}" var="s">
                            <option value="${s.slotId}"><c:out value="${s.name}"/> (<c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/>)</option>
                        </c:forEach>
                    </select>
                    <div class="form-text">
                        Quản lý ca tại <a href="${pageContext.request.contextPath}/admin/time-slots">Khung giờ</a>.
                    </div>
                </div>

                <div class="col-md-3">
                    <label class="form-label">Giáo viên</label>
                    <select class="form-select" name="teacherId" required>
                        <option value="">-- Chọn giáo viên --</option>
                        <c:forEach items="${teachers}" var="tch">
                            <option value="${tch.teacherId}" ${clazz.teacherId == tch.teacherId ? 'selected' : ''}>
                                <c:out value="${tch.fullName}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label">Phòng học</label>
                    <select class="form-select" name="roomId" required>
                        <option value="">-- Chọn phòng --</option>
                        <c:forEach items="${rooms}" var="r">
                            <option value="${r.roomId}" ${clazz.roomId == r.roomId ? 'selected' : ''}>
                                <c:out value="${r.roomName}"/> (<c:out value="${r.roomCode}"/>)
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12">
                    <button class="btn btn-primary" type="submit">Thêm</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card mb-3">
        <div class="card-body">
            <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between">
                <h5 class="card-title m-0">Tạo buổi học (tự động)</h5>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/class-sessions?classId=${clazz.classId}">Xem buổi học</a>
            </div>
            <div class="text-muted mb-3">Tạo các buổi học từ lịch hiện tại. Nếu lớp chưa có ngày kết thúc, hệ thống dùng thời lượng khóa học.</div>
            <form method="post" action="${pageContext.request.contextPath}/admin/class-schedules" class="row g-3">
                <input type="hidden" name="formToken" value="${formToken}">
                <input type="hidden" name="classId" value="${clazz.classId}">
                <input type="hidden" name="action" value="generateSessions">

                <div class="col-md-4">
                    <label class="form-label">Từ ngày (optional)</label>
                    <input class="form-control" type="date" name="fromDate">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Đến ngày (optional)</label>
                    <input class="form-control" type="date" name="toDate">
                </div>
                <div class="col-md-4 d-flex align-items-end">
                    <button class="btn btn-success w-100" type="submit"
                            onclick="return confirm('Tạo buổi học từ lịch cho lớp này?');">
                        Generate class_sessions
                    </button>
                </div>
            </form>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Thứ</th>
                    <th>Ca</th>
                    <th>Giờ</th>
                    <th>Phòng</th>
                    <th>Giáo viên</th>
                    <th class="text-end">Xóa</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${schedules}" var="sc">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${sc.dayOfWeek == 1}">Thứ 2</c:when>
                                <c:when test="${sc.dayOfWeek == 2}">Thứ 3</c:when>
                                <c:when test="${sc.dayOfWeek == 3}">Thứ 4</c:when>
                                <c:when test="${sc.dayOfWeek == 4}">Thứ 5</c:when>
                                <c:when test="${sc.dayOfWeek == 5}">Thứ 6</c:when>
                                <c:when test="${sc.dayOfWeek == 6}">Thứ 7</c:when>
                                <c:otherwise>CN</c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${sc.slotName}"/></td>
                        <td><c:out value="${sc.startTime}"/>-<c:out value="${sc.endTime}"/></td>
                        <td><c:out value="${sc.roomName}"/></td>
                        <td><c:out value="${sc.teacherName}"/></td>
                        <td class="text-end">
                            <form method="post" action="${pageContext.request.contextPath}/admin/class-schedules" class="d-inline">
                                <input type="hidden" name="formToken" value="${formToken}">
                                <input type="hidden" name="classId" value="${clazz.classId}">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="scheduleId" value="${sc.scheduleId}">
                                <button class="btn btn-sm btn-outline-danger" type="submit"
                                        onclick="return confirm('Xóa lịch này?');">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty schedules}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có lịch cho lớp này.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
