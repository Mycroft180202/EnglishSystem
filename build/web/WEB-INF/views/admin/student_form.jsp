<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="s" value="${student}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa thông tin học viên' : 'Thêm học viên'}" active="students">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa thông tin học viên' : 'Thêm học viên'}"/></h3>
        <a class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}${isEdit ? '/students/edit' : '/students/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="studentId" value="${s.studentId}">
        </c:if>
        <c:if test="${not empty formToken}">
            <input type="hidden" name="formToken" value="${formToken}">
        </c:if>

        <div class="col-md-8">
            <label class="form-label">Họ tên</label>
            <input class="form-control" name="fullName" value="${s.fullName}" required maxlength="150">
        </div>

        <div class="col-md-4">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" name="status">
                <option value="ACTIVE" ${empty s.status || s.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="INACTIVE" ${s.status == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động</option>
            </select>
        </div>

        <div class="col-md-4">
            <label class="form-label">Ngày sinh</label>
            <input class="form-control" type="date" name="dob" value="${s.dob}">
        </div>

        <div class="col-md-4">
            <label class="form-label">Giới tính</label>
            <select class="form-select" name="gender">
                <option value="" ${empty s.gender ? 'selected' : ''}>--</option>
                <option value="M" ${s.gender == 'M' ? 'selected' : ''}>Nam</option>
                <option value="F" ${s.gender == 'F' ? 'selected' : ''}>Nữ</option>
            </select>
        </div>

        <div class="col-md-4">
            <label class="form-label">Trình độ đầu vào</label>
            <select class="form-select" name="inputLevel">
                <option value="" ${empty s.inputLevel ? 'selected' : ''}>--</option>
                <option value="A1" ${s.inputLevel == 'A1' ? 'selected' : ''}>A1 &ndash; Beginner</option>
                <option value="A2" ${s.inputLevel == 'A2' ? 'selected' : ''}>A2 &ndash; Elementary</option>
                <option value="B1" ${s.inputLevel == 'B1' ? 'selected' : ''}>B1 &ndash; Intermediate</option>
                <option value="B2" ${s.inputLevel == 'B2' ? 'selected' : ''}>B2 &ndash; Upper Intermediate</option>
                <option value="C1" ${s.inputLevel == 'C1' ? 'selected' : ''}>C1 &ndash; Advanced</option>
                <option value="C2" ${s.inputLevel == 'C2' ? 'selected' : ''}>C2 &ndash; Proficiency</option>
            </select>
            <div class="form-text">Chọn theo khung CEFR: A1, A2, B1, B2, C1, C2.</div>
        </div>

        <div class="col-md-6">
            <label class="form-label">SĐT</label>
            <input class="form-control" name="phone" value="${s.phone}" maxlength="30">
        </div>

        <div class="col-md-6">
            <label class="form-label">Email</label>
            <input class="form-control" name="email" value="${s.email}" maxlength="255">
        </div>

        <div class="col-md-12">
            <label class="form-label">Địa chỉ</label>
            <input class="form-control" name="address" value="${s.address}">
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>
