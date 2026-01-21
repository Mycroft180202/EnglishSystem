<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Báo cáo doanh thu" active="revenue-report">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Doanh thu theo ngày</h3>
    </div>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/accounting/reports/revenue">
        <div class="col-auto">
            <input class="form-control" type="month" name="month" value="${month}">
        </div>
        <div class="col-auto">
            <button class="btn btn-outline-secondary" type="submit">Xem</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Ngày</th>
                    <th>Số giao dịch</th>
                    <th>Doanh thu</th>
                </tr>
            </thead>
            <tbody>
                <c:set var="sum" value="0"/>
                <c:forEach items="${rows}" var="r">
                    <tr>
                        <td><c:out value="${r.day}"/></td>
                        <td><c:out value="${r.paymentCount}"/></td>
                        <td><t:vnd value="${r.total}"/></td>
                    </tr>
                    <c:set var="sum" value="${sum + r.total}"/>
                </c:forEach>
                <c:if test="${empty rows}">
                    <tr>
                        <td colspan="3" class="text-center text-muted">Chưa có dữ liệu thanh toán trong tháng này.</td>
                    </tr>
                </c:if>
            </tbody>
            <c:if test="${not empty rows}">
                <tfoot>
                    <tr>
                        <th colspan="2" class="text-end">Tổng</th>
                        <th><t:vnd value="${sum}"/></th>
                    </tr>
                </tfoot>
            </c:if>
        </table>
    </div>
</t:layout>

