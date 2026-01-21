<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Đăng ký học" active="enrollments">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Đăng ký học</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/enrollments/create">
            Tạo đăng ký
        </a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form class="row g-2 mb-3" method="get" action="">
        <div class="col-auto">
            <input class="form-control" name="studentId" placeholder="StudentId" value="${studentId}">
        </div>
        <div class="col-auto">
            <input class="form-control" name="classId" placeholder="ClassId" value="${classId}">
        </div>
        <div class="col-auto">
            <select class="form-select" name="status">
                <option value="" ${empty status ? 'selected' : ''}>Tất cả</option>
                <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
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
                    <th>Học viên</th>
                    <th>Lớp</th>
                    <th>Khóa</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${enrollments}" var="e" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td>
                            <div><c:out value="${e.studentName}"/></div>
                            <div class="text-muted small"><c:out value="${e.studentPhone}"/></div>
                        </td>
                        <td>
                            <div><c:out value="${e.className}"/></div>
                            <div class="text-muted small"><c:out value="${e.classCode}"/></div>
                        </td>
                        <td><c:out value="${e.courseName}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${e.status}"/></span></td>
                        <td class="text-end">
                            <form class="d-inline" method="post"
                                  action="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/enrollments/status">
                                <input type="hidden" name="id" value="${e.enrollId}">
                                <c:choose>
                                    <c:when test="${e.status == 'ACTIVE'}">
                                        <input type="hidden" name="status" value="CANCELLED">
                                        <button class="btn btn-sm btn-outline-danger" type="submit"
                                                onclick="return confirm('Hủy đăng ký này?');">Hủy</button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="status" value="ACTIVE">
                                        <button class="btn btn-sm btn-outline-success" type="submit">Kích hoạt</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty enrollments}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có đăng ký.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
