<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Yêu cầu thu tiền" active="payment-requests">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Yêu cầu thu tiền</h3>
            <div class="text-muted small">Danh sách yêu cầu do tư vấn gửi, chờ kế toán duyệt.</div>
        </div>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/accounting/payment-requests">
        <input type="hidden" name="formToken" value="${formToken}">
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th style="width:72px;">STT</th>
                        <th>Hóa đơn</th>
                        <th>Học viên</th>
                        <th>Lớp</th>
                        <th>Số tiền</th>
                        <th>Phương thức</th>
                        <th>Gửi lúc</th>
                        <th class="text-end">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${rows}" var="r" varStatus="st">
                        <tr>
                            <td class="text-muted"><c:out value="${st.count}"/></td>
                            <td><c:out value="${r.invoiceCode}"/></td>
                            <td><c:out value="${r.studentName}"/></td>
                            <td><c:out value="${r.className}"/></td>
                            <td><t:vnd value="${r.amount}"/></td>
                            <td><c:out value="${r.method}"/></td>
                            <td><t:date value="${r.createdAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-success" type="submit" name="action" value="approve"
                                        onclick="this.form.requestId.value='${r.requestId}'; return confirm('Duyệt yêu cầu này?');">
                                    Duyệt
                                </button>
                                <button class="btn btn-sm btn-outline-danger" type="submit" name="action" value="reject"
                                        onclick="this.form.requestId.value='${r.requestId}'; return confirm('Từ chối yêu cầu này?');">
                                    Từ chối
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr>
                            <td colspan="8" class="text-center text-muted">Không có yêu cầu nào.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        <input type="hidden" name="requestId" value="">
    </form>
</t:layout>

