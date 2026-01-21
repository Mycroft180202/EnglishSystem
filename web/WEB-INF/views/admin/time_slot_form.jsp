<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="s" value="${slot}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa khung giờ' : 'Thêm khung giờ'}" active="time-slots">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa khung giờ' : 'Thêm khung giờ'}"/></h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/time-slots">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/time-slots/edit' : '/admin/time-slots/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="slotId" value="${s.slotId}">
        </c:if>
        <input type="hidden" name="formToken" value="${formToken}">

        <div class="col-md-6">
            <label class="form-label">Tên ca</label>
            <input class="form-control" name="name" value="${s.name}" required maxlength="50" placeholder="Ví dụ: Ca 1">
        </div>

        <div class="col-md-3">
            <label class="form-label">Bắt đầu</label>
            <input class="form-control" type="time" name="startTime" value="${s.startTime}" required>
        </div>

        <div class="col-md-3">
            <label class="form-label">Kết thúc</label>
            <input class="form-control" type="time" name="endTime" value="${s.endTime}" required>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>

