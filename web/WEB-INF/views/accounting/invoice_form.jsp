<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tạo hóa đơn" active="invoices">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0">Tạo hóa đơn</h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/accounting/invoices">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/accounting/invoices/create"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">

        <div class="col-md-12">
            <label class="form-label">Đăng ký học (Enrollment)</label>
            <select class="form-select" name="enrollId" required>
                <option value="">-- Chọn --</option>
                <c:forEach items="${enrollments}" var="e">
                    <option value="${e.enrollId}" ${enrollId == e.enrollId ? 'selected' : ''}>
                        #<c:out value="${e.enrollId}"/> - <c:out value="${e.studentName}"/> - <c:out value="${e.className}"/> (<c:out value="${e.courseName}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Tổng tiền</label>
            <input class="form-control" name="totalAmount" value="${totalAmount}" placeholder="Ví dụ: 3000000" required>
        </div>
        <div class="col-md-6">
            <label class="form-label">Giảm giá</label>
            <input class="form-control" name="discountAmount" value="${discountAmount}" placeholder="0">
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">Tạo hóa đơn</button>
        </div>
    </form>
</t:layout>

