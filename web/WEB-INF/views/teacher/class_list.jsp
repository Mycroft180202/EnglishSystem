<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Lớp của tôi" active="teacher-classes">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Lớp của tôi</h3>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Mã lớp</th>
                    <th>Tên lớp</th>
                    <th>Khóa học</th>
                    <th>Trạng thái</th>
                    <th class="text-end">Nhập điểm</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${classes}" var="cl">
                    <tr>
                        <td><c:out value="${cl.classCode}"/></td>
                        <td><c:out value="${cl.className}"/></td>
                        <td><c:out value="${cl.courseName}"/></td>
                        <td><span class="badge text-bg-secondary"><c:out value="${cl.status}"/></span></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/teacher/scores?classId=${cl.classId}">Nhập điểm</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty classes}">
                    <tr>
                        <td colspan="5" class="text-center text-muted">Chưa có lớp nào được gán cho bạn.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>

