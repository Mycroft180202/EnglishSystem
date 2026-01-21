<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tài khoản học viên" active="students">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Tài khoản học viên</h3>
            <div class="text-muted"><c:out value="${student.fullName}"/></div>
        </div>
        <a class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <c:if test="${not empty account}">
        <div class="alert alert-info">
            Học viên đã có tài khoản: <b><c:out value="${account.username}"/></b> (status: <c:out value="${account.status}"/>)
        </div>
    </c:if>

    <c:if test="${empty account}">
        <form method="post" action="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/students/account"
              class="row g-3"
              onsubmit="this.querySelector('button[type=submit]').disabled=true;">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="studentId" value="${student.studentId}">

            <div class="col-md-6">
                <label class="form-label">Username</label>
                <input class="form-control" name="username" required maxlength="50">
            </div>
            <div class="col-md-6"></div>

            <div class="col-md-6">
                <label class="form-label">Password</label>
                <input class="form-control" type="password" name="password" required>
            </div>
            <div class="col-md-6">
                <label class="form-label">Confirm password</label>
                <input class="form-control" type="password" name="confirm" required>
            </div>

            <div class="col-12">
                <button class="btn btn-primary" type="submit">Tạo tài khoản STUDENT</button>
            </div>
        </form>
    </c:if>
</t:layout>

