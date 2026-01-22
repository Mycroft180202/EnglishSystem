<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="VietQR (Đã tắt)" active="student-fees">
    <c:set var="i" value="${invoice}"/>
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">VietQR đã được tắt</h3>
            <div class="text-muted">Hóa đơn: <c:out value="${i.invoiceCode}"/></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/fees">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="row g-3">
        <div class="col-md-5">
            <div class="card">
                <div class="card-body">
                    <div class="text-muted">Số tiền cần thanh toán</div>
                    <div class="fs-4"><t:vnd value="${remaining}"/></div>
                    <div class="text-muted small mt-2">Mã tham chiếu: <b><c:out value="${qrRef}"/></b></div>
                    <div class="text-muted small mt-1">
                        Nội dung chuyển khoản: <b><c:out value="${qrInfo}"/></b>
                    </div>
                    <div class="small text-muted mt-2">
                        Hệ thống đã chuyển sang PayOS. Vui lòng quay lại trang học phí và chọn PayOS để thanh toán.
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-7">
            <div class="card">
                <div class="card-body text-center">
                    <div class="text-muted">Không còn hỗ trợ VietQR.</div>
                </div>
            </div>
        </div>
    </div>

    <div class="alert alert-info mt-3">
        Nếu bạn đã thanh toán nhưng hệ thống chưa tự cập nhật, vui lòng đợi vài phút hoặc liên hệ tư vấn/kế toán.
    </div>
</t:layout>
