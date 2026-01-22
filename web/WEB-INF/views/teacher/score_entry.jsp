<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Nhập điểm" active="teacher-scores">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Nhập điểm</h3>
            <div class="text-muted">
                <c:out value="${clazz.className}"/> - <c:out value="${clazz.courseName}"/> (<c:out value="${clazz.classCode}"/>)
            </div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/teacher/sessions">Quay lại</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="alert alert-info">
        Nhập điểm theo từng đầu điểm: Test 1 (20%), Test 2 (30%), Final (40%). Chuyên cần 10% tính tự động từ điểm danh.
    </div>

    <form method="get" action="${pageContext.request.contextPath}/teacher/scores" class="row g-2 mb-3">
        <input type="hidden" name="classId" value="${clazz.classId}">
        <div class="col-auto">
            <select class="form-select" name="assessId" onchange="this.form.submit()">
                <c:forEach items="${assessments}" var="a">
                    <option value="${a.assessId}" ${assessId == a.assessId ? 'selected' : ''}>
                        <c:out value="${a.name}"/> (
                        <c:choose>
                            <c:when test="${a.type == 'TEST1' || a.type == 'QUIZ'}">Test 1</c:when>
                            <c:when test="${a.type == 'TEST2' || a.type == 'MIDTERM'}">Test 2</c:when>
                            <c:when test="${a.type == 'FINAL'}">Final</c:when>
                            <c:otherwise><c:out value="${a.type}"/></c:otherwise>
                        </c:choose>
                        )
                    </option>
                </c:forEach>
            </select>
        </div>
    </form>

    <form method="post" action="${pageContext.request.contextPath}/teacher/scores"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">
        <input type="hidden" name="classId" value="${clazz.classId}">
        <input type="hidden" name="assessId" value="${assessId}">

        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th>Học viên</th>
                        <th>SĐT</th>
                        <th>Điểm</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${enrollments}" var="e">
                        <tr>
                            <td><c:out value="${e.studentName}"/></td>
                            <td><c:out value="${e.studentPhone}"/></td>
                            <td style="max-width: 180px;">
                                <input class="form-control" name="score_${e.enrollId}" value="${scoreMap[e.enrollId]}">
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty enrollments}">
                        <tr>
                            <td colspan="3" class="text-center text-muted">Chưa có học viên hoạt động trong lớp.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <button class="btn btn-primary" type="submit">Lưu điểm</button>
    </form>
</t:layout>
