<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Ví học viên" active="student-wallet">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Ví học viên</h3>
            <div class="text-muted">Số dư hiện tại: <b><t:vnd value="${balance}"/></b></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/classes">Đăng ký học</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <h5 class="card-title">Nạp tiền vào ví</h5>
            <div class="text-muted small mb-2">Nạp tiền online qua PayOS.</div>
            <form method="post" action="${pageContext.request.contextPath}/student/wallet/topup/payos" class="row g-2"
                  onsubmit="this.querySelector('button[type=submit]').disabled=true;">
                <div class="col-md-4">
                    <input class="form-control" name="amount" placeholder="Số tiền (VND)" required>
                </div>
                <div class="col-md-3">
                    <button class="btn btn-primary w-100" type="submit">Nạp PayOS</button>
                </div>
            </form>
        </div>
    </div>
</t:layout>
