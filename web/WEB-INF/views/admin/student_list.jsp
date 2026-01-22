<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Quản lý học viên" active="students">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Quản lý học viên</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students/create">
            Thêm học viên
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
            <input class="form-control" name="q" placeholder="Tìm theo tên/SĐT/email" value="${q}">
        </div>
        <div class="col-auto">
            <select class="form-select" name="status">
                <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="INACTIVE" ${status == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động</option>
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
                    <th>Họ tên</th>
                    <th>Ngày sinh</th>
                    <th>Giới tính</th>
                    <th>SĐT</th>
                    <th>Email</th>
                    <th>Trình độ</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${students}" var="s" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${s.fullName}"/></td>
                        <td><t:date value="${s.dob}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${s.gender == 'M'}">Nam</c:when>
                                <c:when test="${s.gender == 'F'}">Nữ</c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${s.phone}"/></td>
                        <td><c:out value="${s.email}"/></td>
                        <td><c:out value="${s.inputLevel}"/></td>
                        <td>
                            <span class="badge ${s.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'}">
                                <c:choose>
                                    <c:when test="${s.status == 'ACTIVE'}">Hoạt động</c:when>
                                    <c:when test="${s.status == 'INACTIVE'}">Ngưng hoạt động</c:when>
                                    <c:otherwise><c:out value="${s.status}"/></c:otherwise>
                                </c:choose>
                            </span>
                        </td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students/edit?id=${s.studentId}">Sửa</a>
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students/account?studentId=${s.studentId}">Account</a>
                            <c:if test="${sessionScope.authUser.roleCodes.contains('ADMIN')}">
                                <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/students/delete">
                                    <input type="hidden" name="id" value="${s.studentId}">
                                    <button class="btn btn-sm btn-outline-danger" type="submit"
                                            onclick="return confirm('Xóa học viên này?');">Xóa</button>
                                </form>
                            </c:if>
                            <form class="d-inline" method="post"
                                  action="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students/status">
                                <input type="hidden" name="id" value="${s.studentId}">
                                <c:choose>
                                    <c:when test="${s.status == 'ACTIVE'}">
                                        <input type="hidden" name="status" value="INACTIVE">
                                        <button class="btn btn-sm btn-outline-danger" type="submit"
                                                onclick="return confirm('Chuyển học viên sang Ngưng hoạt động?');">
                                            Ngưng hoạt động
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="status" value="ACTIVE">
                                        <button class="btn btn-sm btn-outline-success" type="submit"
                                                onclick="return confirm('Chuyển học viên sang Hoạt động?');">
                                            Hoạt động
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty students}">
                    <tr>
                        <td colspan="9" class="text-center text-muted">Chưa có học viên.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
