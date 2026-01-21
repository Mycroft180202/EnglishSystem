<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Hóa đơn" active="invoices">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Hóa đơn</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/accounting/invoices/create">Tạo hóa đơn</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/accounting/invoices">
        <div class="col-auto">
            <input class="form-control" name="q" placeholder="Tìm theo mã hóa đơn / tên / SĐT" value="${q}">
        </div>
        <div class="col-auto">
            <select class="form-select" name="status">
                <option value="" ${empty status ? 'selected' : ''}>Tất cả</option>
                <option value="UNPAID" ${status == 'UNPAID' ? 'selected' : ''}>UNPAID</option>
                <option value="PARTIAL" ${status == 'PARTIAL' ? 'selected' : ''}>PARTIAL</option>
                <option value="PAID" ${status == 'PAID' ? 'selected' : ''}>PAID</option>
                <option value="VOID" ${status == 'VOID' ? 'selected' : ''}>VOID</option>
            </select>
        </div>
        <div class="col-auto">
            <button class="btn btn-outline-secondary" type="submit">Lọc</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Mã</th>
                    <th>Học viên</th>
                    <th>Lớp</th>
                    <th>Tổng</th>
                    <th>Giảm</th>
                    <th>Đã thu</th>
                    <th>Còn lại</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Chi tiết</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${invoices}" var="i">
                    <tr>
                        <td><c:out value="${i.invoiceCode}"/></td>
                        <td>
                            <div><c:out value="${i.studentName}"/></div>
                            <div class="text-muted small"><c:out value="${i.studentPhone}"/></div>
                        </td>
                        <td>
                            <div><c:out value="${i.className}"/></div>
                            <div class="text-muted small"><c:out value="${i.courseName}"/></div>
                        </td>
                        <td><t:vnd value="${i.totalAmount}"/></td>
                        <td><t:vnd value="${i.discountAmount}"/></td>
                        <td><t:vnd value="${i.paidAmount}"/></td>
                        <td><t:vnd value="${i.remainingAmount}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${i.status}"/></span></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/accounting/invoices/view?id=${i.invoiceId}">Xem</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty invoices}">
                    <tr>
                        <td colspan="9" class="text-center text-muted">Chưa có hóa đơn.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
