<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Yêu cầu rút tiền" active="wallet-withdrawals">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Yêu cầu rút tiền (từ ví)</h3>
            <div class="text-muted small">Tư vấn tạo yêu cầu, kế toán duyệt rồi hệ thống trừ tiền trong ví.</div>
        </div>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/consultant/wallet-withdrawals/create">Tạo yêu cầu</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Học viên</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                    <th>Gửi lúc</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${rows}" var="r" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td>
                            <div class="fw-semibold"><c:out value="${r.studentName}"/></div>
                            <div class="small text-muted"><c:out value="${r.studentPhone}"/></div>
                        </td>
                        <td><t:vnd value="${r.amount}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${r.status == 'PENDING'}"><span class="badge text-bg-secondary">Chờ duyệt</span></c:when>
                                <c:when test="${r.status == 'APPROVED'}"><span class="badge text-bg-success">Đã duyệt</span></c:when>
                                <c:when test="${r.status == 'REJECTED'}"><span class="badge text-bg-danger">Từ chối</span></c:when>
                                <c:otherwise><span class="badge text-bg-light border"><c:out value="${r.status}"/></span></c:otherwise>
                            </c:choose>
                        </td>
                        <td><t:date value="${r.createdAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty rows}">
                    <tr>
                        <td colspan="5" class="text-center text-muted">Chưa có yêu cầu nào.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>

