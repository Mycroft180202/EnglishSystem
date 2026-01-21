<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Học phí" active="student-fees">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Học phí</h3>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Mã</th>
                    <th>Lớp</th>
                    <th>Khóa</th>
                    <th>Tổng</th>
                    <th>Giảm</th>
                    <th>Đã thu</th>
                    <th>Còn lại</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${invoices}" var="i">
                    <tr>
                        <td><c:out value="${i.invoiceCode}"/></td>
                        <td><c:out value="${i.className}"/></td>
                        <td><c:out value="${i.courseName}"/></td>
                        <td><t:vnd value="${i.totalAmount}"/></td>
                        <td><t:vnd value="${i.discountAmount}"/></td>
                        <td><t:vnd value="${i.paidAmount}"/></td>
                        <td><t:vnd value="${i.remainingAmount}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${i.status}"/></span></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty invoices}">
                    <tr>
                        <td colspan="8" class="text-center text-muted">Chưa có hóa đơn.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
