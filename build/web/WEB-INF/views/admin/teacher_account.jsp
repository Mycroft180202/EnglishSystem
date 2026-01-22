<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tài khoản giáo viên" active="teachers">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Tài khoản giáo viên</h3>
            <div class="text-muted"><c:out value="${teacher.fullName}"/></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/teachers">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <c:if test="${not empty account}">
        <div class="alert alert-info">
            Giáo viên đã có tài khoản: <b><c:out value="${account.username}"/></b> (status: <c:out value="${account.status}"/>)
        </div>
    </c:if>

    <c:if test="${empty account}">
        <form method="post" action="${pageContext.request.contextPath}/admin/teachers/account"
              class="row g-3"
              onsubmit="this.querySelector('button[type=submit]').disabled=true;">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="teacherId" value="${teacher.teacherId}">

            <div class="col-12">
                <div class="alert alert-info mb-0">
                    Hệ thống sẽ tự tạo username theo mẫu <b>teacher####</b> và đặt mật khẩu mặc định <b>12345678</b>.
                    Giáo viên bắt buộc đổi mật khẩu khi đăng nhập lần đầu.
                </div>
            </div>

            <div class="col-12">
                <button class="btn btn-primary" type="submit">Tạo tài khoản tự động</button>
            </div>
        </form>
    </c:if>
</t:layout>
