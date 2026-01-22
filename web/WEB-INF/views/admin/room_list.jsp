<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Quản lý phòng học" active="rooms">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Quản lý phòng học</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/rooms/create">Thêm phòng</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/admin/rooms">
        <div class="col-auto">
            <input class="form-control" name="q" placeholder="Tìm kiếm" value="${q}">
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
                    <th>Mã phòng</th>
                    <th>Tên phòng</th>
                    <th>Sức chứa</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${rooms}" var="r" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${r.roomCode}"/></td>
                        <td><c:out value="${r.roomName}"/></td>
                        <td><c:out value="${r.capacity}"/></td>
                        <td>
                            <span class="badge ${r.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'}">
                                <c:choose>
                                    <c:when test="${r.status == 'ACTIVE'}">Hoạt động</c:when>
                                    <c:when test="${r.status == 'INACTIVE'}">Ngưng hoạt động</c:when>
                                    <c:otherwise><c:out value="${r.status}"/></c:otherwise>
                                </c:choose>
                            </span>
                        </td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/admin/rooms/edit?id=${r.roomId}">Sửa</a>
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/rooms/delete">
                                <input type="hidden" name="id" value="${r.roomId}">
                                <button class="btn btn-sm btn-outline-danger" type="submit"
                                        onclick="return confirm('Xóa phòng này?');">Xóa</button>
                            </form>
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/rooms/status">
                                <input type="hidden" name="id" value="${r.roomId}">
                                <c:choose>
                                    <c:when test="${r.status == 'ACTIVE'}">
                                        <input type="hidden" name="status" value="INACTIVE">
                                        <button class="btn btn-sm btn-outline-danger" type="submit"
                                                onclick="return confirm('Chuyển phòng sang Ngưng hoạt động?');">
                                            Ngưng hoạt động
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="status" value="ACTIVE">
                                        <button class="btn btn-sm btn-outline-success" type="submit"
                                                onclick="return confirm('Chuyển phòng sang Hoạt động?');">
                                            Hoạt động
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty rooms}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có phòng học.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
