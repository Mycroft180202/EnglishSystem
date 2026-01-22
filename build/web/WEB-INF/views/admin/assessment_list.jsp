<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Đầu điểm" active="assessments">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Quản lý đầu điểm (Assessments)</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/assessments/create">Thêm đầu điểm</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/admin/assessments">
        <div class="col-auto">
            <select class="form-select" name="courseId" onchange="this.form.submit()">
                <c:forEach items="${courses}" var="c">
                    <option value="${c.courseId}" ${courseId == c.courseId ? 'selected' : ''}>
                        <c:out value="${c.courseName}"/> (<c:out value="${c.courseCode}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>
    </form>

    <div class="alert alert-info">
        Trọng số cố định: Test 1 = 20%, Test 2 = 30%, Final = 40%. Chuyên cần = 10% tính tự động từ điểm danh.
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Tên</th>
                    <th>Type</th>
                    <th>Weight</th>
                    <th>Max</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${assessments}" var="a" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${a.name}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${a.type == 'TEST1' || a.type == 'QUIZ'}">Test 1</c:when>
                                <c:when test="${a.type == 'TEST2' || a.type == 'MIDTERM'}">Test 2</c:when>
                                <c:when test="${a.type == 'FINAL'}">Final Test</c:when>
                                <c:otherwise><c:out value="${a.type}"/></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${a.type == 'TEST1' || a.type == 'QUIZ'}">20</c:when>
                                <c:when test="${a.type == 'TEST2' || a.type == 'MIDTERM'}">30</c:when>
                                <c:when test="${a.type == 'FINAL'}">40</c:when>
                                <c:otherwise><c:out value="${a.weight}"/></c:otherwise>
                            </c:choose>
                            %
                        </td>
                        <td><c:out value="${a.maxScore}"/></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/assessments/edit?id=${a.assessId}">Sửa</a>
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/assessments/delete">
                                <input type="hidden" name="id" value="${a.assessId}">
                                <input type="hidden" name="courseId" value="${courseId}">
                                <button class="btn btn-sm btn-outline-danger" type="submit"
                                        onclick="return confirm('Xóa đầu điểm này?');">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty assessments}">
                    <tr>
                        <td colspan="6" class="text-center text-muted">Chưa có đầu điểm.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
