<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Quản lý lớp học" active="classes">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Quản lý lớp học</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/classes/create">Thêm lớp</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/admin/classes">
        <div class="col-auto">
            <select class="form-select" name="status">
                <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                <option value="OPEN" ${status == 'OPEN' ? 'selected' : ''}>OPEN</option>
                <option value="CLOSED" ${status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
        </div>
        <div class="col-auto">
            <button class="btn btn-outline-secondary" type="submit">Lọc</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Mã lớp</th>
                    <th>Tên lớp</th>
                    <th>Khóa học</th>
                    <th>GV</th>
                    <th>Phòng</th>
                    <th>Sĩ số</th>
                    <th>Bắt đầu</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${classes}" var="cl" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${cl.classCode}"/></td>
                        <td><c:out value="${cl.className}"/></td>
                        <td><c:out value="${cl.courseName}"/></td>
                        <td><c:out value="${cl.teacherName}"/></td>
                        <td><c:out value="${cl.roomName}"/></td>
                        <td><c:out value="${cl.capacity}"/></td>
                        <td><t:date value="${cl.startDate}"/></td>
                        <td>
                            <span class="badge text-bg-secondary"><c:out value="${cl.status}"/></span>
                        </td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/admin/classes/edit?id=${cl.classId}">Sửa</a>
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${pageContext.request.contextPath}/admin/class-schedules?classId=${cl.classId}">Lịch</a>
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${pageContext.request.contextPath}/admin/timetable?classId=${cl.classId}">TKB</a>
                            <div class="btn-group btn-group-sm" role="group">
                                <form method="post" action="${pageContext.request.contextPath}/admin/classes/status">
                                    <input type="hidden" name="id" value="${cl.classId}">
                                    <input type="hidden" name="status" value="OPEN">
                                    <button class="btn btn-outline-success" type="submit">OPEN</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/admin/classes/status">
                                    <input type="hidden" name="id" value="${cl.classId}">
                                    <input type="hidden" name="status" value="CLOSED">
                                    <button class="btn btn-outline-secondary" type="submit">CLOSE</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/admin/classes/status">
                                    <input type="hidden" name="id" value="${cl.classId}">
                                    <input type="hidden" name="status" value="CANCELLED">
                                    <button class="btn btn-outline-danger" type="submit"
                                            onclick="return confirm('Hủy lớp này?');">CANCEL</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty classes}">
                    <tr>
                        <td colspan="10" class="text-center text-muted">Chưa có lớp học.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
