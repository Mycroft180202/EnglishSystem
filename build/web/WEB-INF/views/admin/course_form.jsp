<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="c" value="${course}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa khóa học' : 'Thêm khóa học'}" active="courses">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0">
            <c:out value="${isEdit ? 'Sửa khóa học' : 'Thêm khóa học'}"/>
        </h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/courses">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/courses/edit' : '/admin/courses/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="courseId" value="${c.courseId}">
        </c:if>
        <c:if test="${not empty formToken}">
            <input type="hidden" name="formToken" value="${formToken}">
        </c:if>

        <div class="col-md-4">
            <label class="form-label">Mã khóa học</label>
            <input class="form-control" name="courseCode" value="${c.courseCode}" required maxlength="50">
        </div>

        <div class="col-md-8">
            <label class="form-label">Tên khóa học</label>
            <input class="form-control" name="courseName" value="${c.courseName}" required maxlength="150">
        </div>

        <div class="col-md-6">
            <label class="form-label">Trình độ yêu cầu (CEFR)</label>
            <div class="row g-2">
                <div class="col-6">
                    <select class="form-select" name="levelFrom">
                        <option value="" ${empty levelFrom ? 'selected' : ''}>Từ...</option>
                        <option value="A1" ${levelFrom == 'A1' ? 'selected' : ''}>A1 &ndash; Beginner</option>
                        <option value="A2" ${levelFrom == 'A2' ? 'selected' : ''}>A2 &ndash; Elementary</option>
                        <option value="B1" ${levelFrom == 'B1' ? 'selected' : ''}>B1 &ndash; Intermediate</option>
                        <option value="B2" ${levelFrom == 'B2' ? 'selected' : ''}>B2 &ndash; Upper Intermediate</option>
                        <option value="C1" ${levelFrom == 'C1' ? 'selected' : ''}>C1 &ndash; Advanced</option>
                        <option value="C2" ${levelFrom == 'C2' ? 'selected' : ''}>C2 &ndash; Proficiency</option>
                    </select>
                </div>
                <div class="col-6">
                    <select class="form-select" name="levelTo">
                        <option value="" ${empty levelTo ? 'selected' : ''}>Đến...</option>
                        <option value="A1" ${levelTo == 'A1' ? 'selected' : ''}>A1 &ndash; Beginner</option>
                        <option value="A2" ${levelTo == 'A2' ? 'selected' : ''}>A2 &ndash; Elementary</option>
                        <option value="B1" ${levelTo == 'B1' ? 'selected' : ''}>B1 &ndash; Intermediate</option>
                        <option value="B2" ${levelTo == 'B2' ? 'selected' : ''}>B2 &ndash; Upper Intermediate</option>
                        <option value="C1" ${levelTo == 'C1' ? 'selected' : ''}>C1 &ndash; Advanced</option>
                        <option value="C2" ${levelTo == 'C2' ? 'selected' : ''}>C2 &ndash; Proficiency</option>
                    </select>
                </div>
            </div>
            <div class="form-text">
                Ví dụ: chọn Từ A1 đến B1.
                <c:if test="${empty levelFrom && empty levelTo && not empty c.level}">
                    (Giá trị hiện tại: <c:out value="${c.level}"/>)
                </c:if>
            </div>
        </div>

        <div class="col-md-3">
            <label class="form-label">Thời lượng (tuần)</label>
            <input class="form-control" name="durationWeeks" type="number" min="1" value="${empty c.durationWeeks ? 1 : c.durationWeeks}" required>
        </div>

        <div class="col-md-3">
            <label class="form-label">Học phí chuẩn</label>
            <input class="form-control" name="standardFee" type="number" min="0" step="0.01" value="${empty c.standardFee ? 0 : c.standardFee}" required>
        </div>

        <div class="col-md-12">
            <label class="form-label">Mô tả</label>
            <textarea class="form-control" name="description" rows="4"><c:out value="${c.description}"/></textarea>
        </div>

        <div class="col-md-4">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" name="status">
                <option value="ACTIVE" ${empty c.status || c.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                <option value="INACTIVE" ${c.status == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động</option>
            </select>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
</t:layout>
