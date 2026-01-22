<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Tạo đăng ký học" active="enrollments">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0">Tạo đăng ký học</h3>
        <a class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}${pageContext.request.requestURI.contains('/admin/') ? '/admin' : '/consultant'}/enrollments">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action=""
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
        <input type="hidden" name="formToken" value="${formToken}">

        <div class="col-md-6">
            <label class="form-label">Học viên</label>
            <select class="form-select" name="studentId" required>
                <option value="">-- Chọn học viên --</option>
                <c:forEach items="${students}" var="s">
                    <option value="${s.studentId}" ${studentId == s.studentId ? 'selected' : ''}>
                        <c:out value="${s.fullName}"/> (<c:out value="${s.phone}"/>)
                    </option>
                </c:forEach>
            </select>
            <div class="form-text">
                Nếu chưa có học viên, tạo tại mục Học viên trước.
            </div>
        </div>

        <div class="col-md-6">
            <label class="form-label">Lớp học</label>
            <select class="form-select" name="classId" required>
                <option value="">-- Chọn lớp --</option>
                <c:forEach items="${classes}" var="cl">
                    <option value="${cl.classId}" ${classId == cl.classId ? 'selected' : ''}>
                        <c:out value="${cl.className}"/> - <c:out value="${cl.courseName}"/> (<c:out value="${cl.status}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">Đăng ký</button>
        </div>
    </form>
</t:layout>
