<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Kết quả học tập" active="student-results">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Kết quả học tập</h3>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Lớp</th>
                    <th>Khóa</th>
                    <th>Đầu điểm</th>
                    <th>Type</th>
                    <th>Điểm</th>
                    <th>Max</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${rows}" var="r">
                    <tr>
                        <td><c:out value="${r.className}"/></td>
                        <td><c:out value="${r.courseName}"/></td>
                        <td><c:out value="${r.assessmentName}"/></td>
                        <td><c:out value="${r.assessmentType}"/></td>
                        <td><c:out value="${r.scoreValue}"/></td>
                        <td><c:out value="${r.maxScore}"/></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty rows}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có dữ liệu điểm.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
