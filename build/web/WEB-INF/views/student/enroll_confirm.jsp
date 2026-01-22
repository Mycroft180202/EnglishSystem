<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Xác nhận đăng ký" active="student-classes">
    <c:set var="c" value="${clazz}"/>

    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Xác nhận đăng ký</h3>
            <div class="text-muted">
                <c:out value="${empty c.classCode ? c.className : c.classCode}"/> - <c:out value="${c.courseName}"/>
            </div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/classes">Quay lại</a>
    </div>

    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>
    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>

    <div class="row g-3 mb-3">
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Học phí</div>
                <div class="fs-5"><t:vnd value="${c.standardFee}"/></div>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Số dư ví</div>
                <div class="fs-5"><t:vnd value="${balance}"/></div>
            </div></div>
        </div>
        <div class="col-md-4">
            <div class="card"><div class="card-body">
                <div class="text-muted">Thời gian</div>
                <div class="fs-6"><t:date value="${c.startDate}"/> - <t:date value="${c.endDate}"/></div>
            </div></div>
        </div>
    </div>

    <div class="card mb-3">
        <div class="card-body">
            <h5 class="card-title">Lịch học</h5>
            <div class="small text-muted mb-2">Lịch theo tuần (day/slot) của lớp.</div>
            <div class="d-flex flex-wrap gap-2">
                <c:forEach items="${schedules}" var="s">
                    <span class="badge text-bg-light border">
                        D<c:out value="${s.dayOfWeek}"/> - <c:out value="${s.slotName}"/> (<c:out value="${s.startTime}"/>-<c:out value="${s.endTime}"/>)
                    </span>
                </c:forEach>
                <c:if test="${empty schedules}">
                    <span class="text-muted">Chưa có lịch học.</span>
                </c:if>
            </div>
        </div>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/student/enroll" class="card">
        <div class="card-body">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="classId" value="${c.classId}">

            <div class="mb-2 fw-semibold">Phương thức thanh toán</div>
            <div class="form-check">
                <input class="form-check-input" type="radio" name="pay" id="payWallet" value="WALLET" checked>
                <label class="form-check-label" for="payWallet">Trừ tiền từ ví (tự động)</label>
            </div>
            <div class="form-check">
                <input class="form-check-input" type="radio" name="pay" id="payPayos" value="PAYOS">
                <label class="form-check-label" for="payPayos">Thanh toán online bằng PayOS</label>
            </div>
            <div class="form-check">
                <input class="form-check-input" type="radio" name="pay" id="payConsultant" value="CONSULTANT">
                <label class="form-check-label" for="payConsultant">Nộp tiền qua tư vấn (chờ kế toán xác nhận)</label>
            </div>

            <div class="mt-3">
                <button class="btn btn-primary" type="submit">Xác nhận đăng ký</button>
            </div>
        </div>
    </form>
</t:layout>
