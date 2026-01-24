<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tạo yêu cầu rút tiền" active="wallet-withdrawals">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Tạo yêu cầu rút tiền</h3>
            <div class="text-muted small">Chỉ rút tiền mặt tại trung tâm. Kế toán phải duyệt.</div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/consultant/wallet-withdrawals">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/consultant/wallet-withdrawals/create" class="card"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <div class="card-body">
            <input type="hidden" name="formToken" value="${formToken}">

            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">Mã học viên (student_id)</label>
                    <input class="form-control" name="studentId" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Số tiền</label>
                    <input class="form-control" name="amount" required>
                </div>
                <div class="col-md-12">
                    <label class="form-label">Ghi chú (optional)</label>
                    <input class="form-control" name="note" maxlength="255">
                </div>
            </div>

            <div class="mt-3">
                <button class="btn btn-primary" type="submit">Gửi cho kế toán duyệt</button>
            </div>
        </div>
    </form>
</t:layout>

