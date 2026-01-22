<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
                    <th style="width:72px;">Buổi</th>
                    <th>Ngày</th>
                    <th>Ca</th>
                    <th>Giờ</th>
                    <th>Phòng</th>
                    <th>Giáo viên</th>
                    <th>Check-in GV</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${sessions}" var="s">
                    <tr>
                        <td class="text-muted"><c:out value="${s.sessionNo}"/></td>
                        <td><t:date value="${s.sessionDate}"/></td>
                        <td><c:out value="${s.slotName}"/></td>
                        <td><c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/></td>
                        <td><c:out value="${s.roomName}"/></td>
                        <td><c:out value="${s.teacherName}"/></td>
                        <td class="small text-muted"><c:out value="${s.teacherCheckinAt}"/></td>
                        <td>
                            <span class="badge ${s.status == 'CANCELLED' ? 'text-bg-danger' : 'text-bg-secondary'}">
                                <c:out value="${s.status}"/>
                            </span>
                        </td>
                        <td class="text-end" style="min-width: 340px;">
                            <c:if test="${s.status == 'SCHEDULED' || s.status == 'CANCELLED'}">
                                <details class="d-inline-block text-start">
                                    <summary class="btn btn-sm btn-outline-primary">Đổi lịch</summary>
                                    <form class="border rounded p-2 mt-2 bg-body" method="post" action="${pageContext.request.contextPath}/admin/class-sessions/update">
                                        <input type="hidden" name="action" value="reschedule">
                                        <input type="hidden" name="classId" value="${clazz.classId}">
                                        <input type="hidden" name="sessionId" value="${s.sessionId}">
                                        <div class="row g-2">
                                            <div class="col-12 col-md-4">
                                                <input class="form-control form-control-sm" type="date" name="newDate" value="${s.sessionDate}">
                                            </div>
                                            <div class="col-12 col-md-4">
                                                <select class="form-select form-select-sm" name="newSlotId">
                                                    <c:forEach items="${slots}" var="sl">
                                                        <option value="${sl.slotId}" ${sl.slotId == s.slotId ? 'selected' : ''}>
                                                            <c:out value="${sl.name}"/> (<c:out value="${sl.startTime}"/>-<c:out value="${sl.endTime}"/>)
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-12 col-md-4">
                                                <select class="form-select form-select-sm" name="newRoomId">
                                                    <c:forEach items="${rooms}" var="r">
                                                        <option value="${r.roomId}" ${r.roomId == s.roomId ? 'selected' : ''}>
                                                            <c:out value="${r.roomName}"/> (<c:out value="${r.roomCode}"/>)
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-12 text-end">
                                                <button class="btn btn-sm btn-primary" type="submit"
                                                        onclick="return confirm('Đổi lịch buổi #${s.sessionNo}?');">Lưu</button>
                                            </div>
                                        </div>
                                    </form>
                                </details>
                                <c:if test="${s.status == 'SCHEDULED'}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/class-sessions/update">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="classId" value="${clazz.classId}">
                                        <input type="hidden" name="sessionId" value="${s.sessionId}">
                                        <button class="btn btn-sm btn-outline-danger" type="submit"
                                                onclick="return confirm('Hủy buổi #${s.sessionNo}?');">Hủy</button>
                                    </form>
                                </c:if>
                            </c:if>
                            <c:if test="${s.status != 'SCHEDULED' && s.status != 'CANCELLED'}">
                                <span class="text-muted">--</span>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty sessions}">
                    <tr>
                        <td colspan="9" class="text-center text-muted">Chưa có buổi học. Hãy Generate từ Lịch học.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
