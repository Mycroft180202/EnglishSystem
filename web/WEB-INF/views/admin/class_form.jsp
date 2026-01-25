<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<c:set var="cl" value="${clazz}"/>
<c:set var="isEdit" value="${mode == 'edit'}"/>

<t:layout title="${isEdit ? 'Sửa lớp học' : 'Thêm lớp học'}" active="classes">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <h3 class="m-0"><c:out value="${isEdit ? 'Sửa lớp học' : 'Thêm lớp học'}"/></h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/classes">Quay lại</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${isEdit ? '/admin/classes/edit' : '/admin/classes/create'}"
          class="row g-3"
          onsubmit="this.querySelector('button[type=submit]').disabled=true;">

        <c:if test="${isEdit}">
            <input type="hidden" name="classId" value="${cl.classId}">
        </c:if>
        <c:if test="${not empty formToken}">
            <input type="hidden" name="formToken" value="${formToken}">
        </c:if>

        <div class="col-md-4">
            <label class="form-label">Mã lớp</label>
            <input class="form-control" name="classCode" value="${cl.classCode}" required maxlength="50">
        </div>

        <div class="col-md-8">
            <label class="form-label">Tên lớp</label>
            <input class="form-control" name="className" value="${cl.className}" required maxlength="150">
        </div>

        <div class="col-md-6">
            <label class="form-label">Khóa học</label>
            <select class="form-select" name="courseId" required id="courseId">
                <option value="">-- Chọn khóa học --</option>
                <c:forEach items="${courses}" var="c">
                    <option value="${c.courseId}" data-weeks="${c.durationWeeks}" ${cl.courseId == c.courseId ? 'selected' : ''}>
                        <c:out value="${c.courseName}"/> (<c:out value="${c.courseCode}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-md-3">
            <label class="form-label">Sĩ số tối đa</label>
            <input class="form-control" type="number" min="1" name="capacity" value="${empty cl.capacity ? 1 : cl.capacity}" required>
        </div>

        <div class="col-md-3">
            <label class="form-label">Trạng thái</label>
            <select class="form-select" name="status">
                <option value="DRAFT" ${empty cl.status || cl.status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                <option value="OPEN" ${cl.status == 'OPEN' ? 'selected' : ''}>OPEN</option>
                <option value="CLOSED" ${cl.status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                <option value="CANCELLED" ${cl.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Giáo viên (tùy chọn)</label>
            <select class="form-select" name="teacherId">
                <option value="">-- Chưa gán --</option>
                <c:forEach items="${teachers}" var="tch">
                    <option value="${tch.teacherId}" ${cl.teacherId == tch.teacherId ? 'selected' : ''}>
                        <c:out value="${tch.fullName}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Phòng học (tùy chọn)</label>
            <select class="form-select" name="roomId">
                <option value="">-- Chưa gán --</option>
                <c:forEach items="${rooms}" var="r">
                    <option value="${r.roomId}" ${cl.roomId == r.roomId ? 'selected' : ''}>
                        <c:out value="${r.roomName}"/> (<c:out value="${r.roomCode}"/>)
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="col-md-6">
            <label class="form-label">Ngày bắt đầu</label>
            <input class="form-control" type="date" name="startDate" value="${cl.startDate}" required id="startDate">
        </div>

        <div class="col-md-6">
            <label class="form-label">Ngày kết thúc (tùy chọn)</label>
            <input class="form-control" type="date" name="endDate" value="${cl.endDate}" id="endDate">
            <div class="form-text">Nếu để trống, hệ thống sẽ tự tính theo thời lượng khóa học.</div>
        </div>

        <div class="col-12">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" id="autoEndDate" name="autoEndDate" value="1">
                <label class="form-check-label" for="autoEndDate">
                    Tự tính lại ngày kết thúc theo số tuần của khóa học
                </label>
            </div>
            <div class="form-text">
                Bật tùy chọn này để hệ thống tính lại ngày kết thúc = ngày bắt đầu + (số tuần * 7 - 1) ngày.
            </div>
        </div>

        <div class="col-12">
            <button class="btn btn-primary" type="submit">${isEdit ? 'Lưu' : 'Tạo mới'}</button>
        </div>
    </form>
    <script>
        (function () {
            const courseEl = document.getElementById('courseId');
            const startEl = document.getElementById('startDate');
            const endEl = document.getElementById('endDate');
            if (!courseEl || !startEl || !endEl) return;

            function parseYmd(s) {
                const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s || '');
                if (!m) return null;
                return new Date(Date.UTC(Number(m[1]), Number(m[2]) - 1, Number(m[3])));
            }

            function fmtYmd(dt) {
                const y = dt.getUTCFullYear();
                const m = String(dt.getUTCMonth() + 1).padStart(2, '0');
                const d = String(dt.getUTCDate()).padStart(2, '0');
                return `${y}-${m}-${d}`;
            }

            function autoFillEndDate() {
                const auto = autoEndEl && autoEndEl.checked;
                if (!auto && endEl.value) return;
                const opt = courseEl.options[courseEl.selectedIndex];
                const weeks = Number((opt && opt.dataset && opt.dataset.weeks) || 0);
                if (!weeks || weeks <= 0) return;
                const start = parseYmd(startEl.value);
                if (!start) return;
                const end = new Date(start.getTime() + ((weeks * 7 - 1) * 24 * 60 * 60 * 1000));
                endEl.value = fmtYmd(end);
            }

            const autoEndEl = document.getElementById('autoEndDate');
            courseEl.addEventListener('change', autoFillEndDate);
            startEl.addEventListener('change', autoFillEndDate);
            if (autoEndEl) autoEndEl.addEventListener('change', autoFillEndDate);
            autoFillEndDate();
        })();
    </script>
</t:layout>
