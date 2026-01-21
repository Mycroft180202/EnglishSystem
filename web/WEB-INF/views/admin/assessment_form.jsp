<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="a" value="${assessment}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa đầu điểm' : 'Thêm đầu điểm'}" active="assessments">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa đầu điểm' : 'Thêm đầu điểm'}"/></h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/assessments">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/assessments/edit' : '/admin/assessments/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="assessId" value="${a.assessId}">
        </c:if>
        <input type="hidden" name="formToken" value="${formToken}">

        <div class="col-md-6">
            <label class="form-label">Khóa học</label>
            <select class="form-select" name="courseId" required>
                <option value="">-- Chọn khóa học --</option>
                <c:forEach items="${courses}" var="c">
                    <option value="${c.courseId}" ${a.courseId == c.courseId ? 'selected' : ''}>
                        <c:out value="${c.courseName}"/> (<c:out value="${c.courseCode}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Type</label>
            <select class="form-select" name="type" required>
                <option value="QUIZ" ${empty a.type || a.type == 'QUIZ' ? 'selected' : ''}>QUIZ</option>
                <option value="MIDTERM" ${a.type == 'MIDTERM' ? 'selected' : ''}>MIDTERM</option>
                <option value="FINAL" ${a.type == 'FINAL' ? 'selected' : ''}>FINAL</option>
                <option value="OTHER" ${a.type == 'OTHER' ? 'selected' : ''}>OTHER</option>
            </select>
        </div>

        <div class="col-md-8">
            <label class="form-label">Tên đầu điểm</label>
            <input class="form-control" name="name" value="${a.name}" required maxlength="150">
        </div>

        <div class="col-md-2">
            <label class="form-label">Weight</label>
            <input class="form-control" name="weight" type="number" min="0" max="100" step="0.01" value="${empty a.weight ? 0 : a.weight}">
        </div>

        <div class="col-md-2">
            <label class="form-label">Max</label>
            <input class="form-control" name="maxScore" type="number" min="0.01" step="0.01" value="${empty a.maxScore ? 10 : a.maxScore}">
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>

