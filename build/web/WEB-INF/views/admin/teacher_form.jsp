<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="tch" value="${teacher}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa thông tin giáo viên' : 'Thêm giáo viên'}" active="teachers">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa thông tin giáo viên' : 'Thêm giáo viên'}"/></h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/teachers">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/teachers/edit' : '/admin/teachers/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="teacherId" value="${tch.teacherId}">
        </c:if>
        <c:if test="${not empty formToken}">
            <input type="hidden" name="formToken" value="${formToken}">
        </c:if>

        <div class="col-md-8">
            <label class="form-label">Họ tên</label>
            <input class="form-control" name="fullName" value="${tch.fullName}" required maxlength="150">
        </div>

        <div class="col-md-4">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" name="status">
                <option value="ACTIVE" ${empty tch.status || tch.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="INACTIVE" ${tch.status == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động</option>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Email</label>
            <input class="form-control" name="email" value="${tch.email}" maxlength="255">
        </div>

        <div class="col-md-6">
            <label class="form-label">SĐT</label>
            <input class="form-control" name="phone" value="${tch.phone}" maxlength="30">
        </div>

        <div class="col-md-12">
            <label class="form-label">Trình độ / chứng chỉ</label>
            <select class="form-select" name="level">
                <option value="" ${empty tch.level ? 'selected' : ''}>--</option>
                <option value="A1" ${tch.level == 'A1' ? 'selected' : ''}>A1 &ndash; Beginner</option>
                <option value="A2" ${tch.level == 'A2' ? 'selected' : ''}>A2 &ndash; Elementary</option>
                <option value="B1" ${tch.level == 'B1' ? 'selected' : ''}>B1 &ndash; Intermediate</option>
                <option value="B2" ${tch.level == 'B2' ? 'selected' : ''}>B2 &ndash; Upper Intermediate</option>
                <option value="C1" ${tch.level == 'C1' ? 'selected' : ''}>C1 &ndash; Advanced</option>
                <option value="C2" ${tch.level == 'C2' ? 'selected' : ''}>C2 &ndash; Proficiency</option>
            </select>
            <div class="form-text">Chọn theo khung CEFR: A1, A2, B1, B2, C1, C2.</div>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>
