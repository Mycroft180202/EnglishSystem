<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Đăng ký lớp học" active="student-classes">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Lớp học có thể đăng ký</h3>
            <div class="text-muted">Số dư ví: <b><t:vnd value="${balance}"/></b></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/student/timetable">Lịch học</a>
    </div>

    <c:if test="${not empty myEnrollments}">
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title mb-3">Lop ban dang hoc</h5>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                            <tr>
                                <th>Mon / Lop</th>
                                <th>Trang thai</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${myEnrollments}" var="e">
                                <tr>
                                    <td>
                                        <div class="fw-semibold"><c:out value="${empty e.classCode ? e.className : e.classCode}"/></div>
                                        <div class="small text-muted"><c:out value="${e.courseName}"/></div>
                                    </td>
                                    <td><span class="badge text-bg-success"><c:out value="${e.status}"/></span></td>
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
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td>
                            <div class="fw-semibold"><c:out value="${empty c.classCode ? c.className : c.classCode}"/></div>
                            <div class="small text-muted"><c:out value="${c.className}"/></div>
                        </td>
                        <td><c:out value="${c.courseName}"/></td>
                        <td><t:vnd value="${c.standardFee}"/></td>
                        <td>
                            <t:date value="${c.startDate}"/> - <t:date value="${c.endDate}"/>
                        </td>
                        <td>
                            <c:out value="${c.activeEnrollCount}"/>/<c:out value="${c.capacity}"/>
                        </td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/student/enroll?classId=${c.classId}">Chọn</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty classes}">
                    <tr>
                        <td colspan="7" class="text-center text-muted">Hiện chưa có lớp OPEN.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
