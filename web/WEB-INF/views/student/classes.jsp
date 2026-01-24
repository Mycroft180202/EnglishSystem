<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Đăng ký học" active="student-classes">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Lớp học có thể đăng ký</h3>
            <div class="text-muted">Số dư ví: <b><t:vnd value="${balance}"/></b></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/timetable">Lịch học</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <c:if test="${not empty myRegistrations}">
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title mb-3">Lớp bạn đã đăng ký</h5>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                            <tr>
                                <th>Môn / Lớp</th>
                                <th>Thời gian</th>
                                <th>Thời khóa biểu</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${myRegistrations}" var="e">
                                <tr>
                                    <td>
                                        <div class="fw-semibold"><c:out value="${empty e.classCode ? e.className : e.classCode}"/></div>
                                        <div class="small text-muted"><c:out value="${e.courseName}"/></div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty e.classStartDate && not empty e.classEndDate}">
                                                <t:date value="${e.classStartDate}"/> - <t:date value="${e.classEndDate}"/>
                                            </c:when>
                                            <c:when test="${not empty e.classStartDate}">
                                                <t:date value="${e.classStartDate}"/>
                                            </c:when>
                                            <c:otherwise>--</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${e.hasSchedule}">
                                                <span class="badge text-bg-success">Có TKB</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge text-bg-warning">Chưa có TKB</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${e.status == 'ACTIVE'}"><span class="badge text-bg-success">Đang học</span></c:when>
                                            <c:when test="${e.status == 'PENDING'}"><span class="badge text-bg-secondary">Chờ duyệt</span></c:when>
                                            <c:when test="${e.status == 'COMPLETED'}"><span class="badge text-bg-primary">Hoàn thành</span></c:when>
                                            <c:when test="${e.status == 'CANCELLED'}"><span class="badge text-bg-danger">Đã hủy</span></c:when>
                                            <c:otherwise><span class="badge text-bg-light border"><c:out value="${e.status}"/></span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Lớp</th>
                    <th>Khóa học</th>
                    <th>Học phí</th>
                    <th>Thời gian</th>
                    <th>Sĩ số</th>
                    <th class="text-end">Đăng ký</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${classes}" var="c" varStatus="st">
                    <c:set var="enr" value="${enrollByClass[c.classId]}"/>
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td>
                            <div class="fw-semibold"><c:out value="${empty c.classCode ? c.className : c.classCode}"/></div>
                            <div class="small text-muted"><c:out value="${c.className}"/></div>
                        </td>
                        <td><c:out value="${c.courseName}"/></td>
                        <td><t:vnd value="${c.standardFee}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty c.startDate && not empty c.endDate}">
                                    <t:date value="${c.startDate}"/> - <t:date value="${c.endDate}"/>
                                </c:when>
                                <c:when test="${not empty c.startDate}">
                                    <t:date value="${c.startDate}"/>
                                </c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:out value="${c.activeEnrollCount}"/>/<c:out value="${c.capacity}"/>
                        </td>
                        <td class="text-end">
                            <c:choose>
                                <c:when test="${empty enr}">
                                    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/student/enroll?classId=${c.classId}">Chọn</a>
                                </c:when>
                                <c:when test="${enr.status == 'PENDING'}">
                                    <span class="badge text-bg-secondary">Đã đăng ký (Chờ duyệt)</span>
                                </c:when>
                                <c:when test="${enr.status == 'ACTIVE'}">
                                    <span class="badge text-bg-success">Đã đăng ký</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge text-bg-light border"><c:out value="${enr.status}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty classes}">
                    <tr>
                        <td colspan="7" class="text-center text-muted">Hiện chưa có lớp đang mở.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
