<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Gửi yêu cầu thu tiền" active="enrollments">
    <c:set var="i" value="${invoice}"/>
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Gửi yêu cầu thu tiền</h3>
            <div class="text-muted">Hóa đơn: <c:out value="${i.invoiceCode}"/></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/consultant/enrollments">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="row g-3 mb-3">
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Tổng</div>
                <div class="fs-6"><t:vnd value="${i.totalAmount}"/></div>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Đã thu</div>
                <div class="fs-6"><t:vnd value="${i.paidAmount}"/></div>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Còn lại</div>
                <div class="fs-6"><t:vnd value="${remaining}"/></div>
            </div></div>
        </div>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/consultant/payment-requests/create" class="card"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <div class="card-body">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="invoiceId" value="${i.invoiceId}">

            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">Số tiền</label>
                    <input class="form-control" name="amount" value="${remaining}" required>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Phương thức</label>
                    <select class="form-select" name="method">
                        <option value="CASH">CASH</option>
                        <%-- TRANSFER disabled: only CASH (accounting approval) and online (PayOS) --%>
                    </select>
                </div>
                <div class="col-md-12">
                    <label class="form-label">Ghi chú (optional)</label>
                    <input class="form-control" name="note" maxlength="255">
                </div>
            </div>

            <div class="mt-3">
                <button class="btn btn-primary" type="submit">Gửi cho kế toán</button>
            </div>
        </div>
    </form>
</t:layout>
