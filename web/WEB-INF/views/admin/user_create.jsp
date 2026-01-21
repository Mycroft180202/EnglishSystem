<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tạo tài khoản nhân viên" active="admin-users">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0">Tạo tài khoản nhân viên</h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/app/home">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/admin/users/create"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">

        <div class="col-md-6">
            <label class="form-label">Username</label>
            <input class="form-control" name="username" required maxlength="50" value="${username}">
        </div>

        <div class="col-md-6">
            <label class="form-label">Role</label>
            <select class="form-select" name="role">
                <option value="CONSULTANT" ${empty role || role == 'CONSULTANT' ? 'selected' : ''}>CONSULTANT</option>
                <option value="ACCOUNTANT" ${role == 'ACCOUNTANT' ? 'selected' : ''}>ACCOUNTANT</option>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" name="password" required>
        </div>
        <div class="col-md-6">
            <label class="form-label">Confirm password</label>
            <input class="form-control" type="password" name="confirm" required>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">Tạo tài khoản</button>
        </div>
    </form>
</t:layout>

