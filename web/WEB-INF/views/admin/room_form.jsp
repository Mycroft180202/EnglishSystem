<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="r" value="${room}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa phòng học' : 'Thêm phòng học'}" active="rooms">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa phòng học' : 'Thêm phòng học'}"/></h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/rooms">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/rooms/edit' : '/admin/rooms/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="roomId" value="${r.roomId}">
        </c:if>
        <c:if test="${not empty formToken}">
            <input type="hidden" name="formToken" value="${formToken}">
        </c:if>

        <div class="col-md-4">
            <label class="form-label">Mã phòng</label>
            <input class="form-control" name="roomCode" value="${r.roomCode}" required maxlength="50">
        </div>

        <div class="col-md-5">
            <label class="form-label">Tên phòng</label>
            <input class="form-control" name="roomName" value="${r.roomName}" required maxlength="100">
        </div>

        <div class="col-md-3">
            <label class="form-label">Sức chứa</label>
            <input class="form-control" type="number" min="1" name="capacity" value="${empty r.capacity ? 1 : r.capacity}" required>
        </div>

        <div class="col-md-4">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" name="status">
                <option value="ACTIVE" ${empty r.status || r.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="INACTIVE" ${r.status == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động</option>
            </select>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>
