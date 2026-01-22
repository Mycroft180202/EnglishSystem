<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@page import="Controller.PaymentAddServlet"%>

<t:layout title="Chi tiết hóa đơn" active="invoices">
    <c:set var="i" value="${invoice}"/>
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Hóa đơn: <c:out value="${i.invoiceCode}"/></h3>
            <div class="text-muted"><c:out value="${i.studentName}"/> - <c:out value="${i.className}"/></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/accounting/invoices">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="row g-3 mb-3">
        <div class="col-md-3">
            <div class="card"><div class="card-body">
                <div class="text-muted">Tổng</div>
                <div class="fs-5"><t:vnd value="${i.totalAmount}"/></div>
            </div></div>
        </div>
        <div class="col-md-3">
            <div class="card"><div class="card-body">
                <div class="text-muted">Giảm</div>
                <div class="fs-5"><t:vnd value="${i.discountAmount}"/></div>
            </div></div>
        </div>
        <div class="col-md-3">
            <div class="card"><div class="card-body">
                <div class="text-muted">Đã thu</div>
                <div class="fs-5"><t:vnd value="${i.paidAmount}"/></div>
            </div></div>
        </div>
        <div class="col-md-3">
            <div class="card"><div class="card-body">
                <div class="text-muted">Trạng thái</div>
                <div class="fs-5"><c:out value="${i.status}"/></div>
            </div></div>
        </div>
    </div>

    <div class="card mb-3">
        <div class="card-body">
            <h5 class="card-title">Ghi nhận thanh toán</h5>
            <form method="post" action="${pageContext.request.contextPath}/accounting/payments/add" class="row g-2">
                <input type="hidden" name="invoiceId" value="${i.invoiceId}">
                <input type="hidden" name="formToken" value="<%= PaymentAddServlet.issueToken(request) %>">
                <div class="col-md-3">
                    <input class="form-control" name="amount" placeholder="Số tiền" required>
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="method">
                        <option value="CASH">CASH</option>
                        <option value="TRANSFER">TRANSFER</option>
                        <option value="CARD">CARD</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <input class="form-control" name="txnRef" placeholder="Mã giao dịch (optional)">
                </div>
                <div class="col-md-2">
                    <button class="btn btn-success w-100" type="submit">Thu</button>
                </div>
            </form>
        </div>
    </div>

    <h5>Danh sách thanh toán</h5>
    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Số tiền</th>
                    <th>Phương thức</th>
                    <th>TxnRef</th>
                    <th>PaidAt</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${payments}" var="p" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><t:vnd value="${p.amount}"/></td>
                        <td><c:out value="${p.method}"/></td>
                        <td><c:out value="${p.txnRef}"/></td>
                        <td><t:date value="${p.paidAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty payments}">
                    <tr>
                        <td colspan="5" class="text-center text-muted">Chưa có thanh toán.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
