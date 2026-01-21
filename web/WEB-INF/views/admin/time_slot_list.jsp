<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Khung giờ học" active="time-slots">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Khung giờ học</h3>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/time-slots/create">Thêm khung giờ</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th style="width:72px;">STT</th>
                    <th>Tên ca</th>
                    <th>Bắt đầu</th>
                    <th>Kết thúc</th>
                    <th class="text-end">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${slots}" var="s" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${s.name}"/></td>
                        <td><c:out value="${s.startTime}"/></td>
                        <td><c:out value="${s.endTime}"/></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/time-slots/edit?id=${s.slotId}">Sửa</a>
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/time-slots/delete">
                                <input type="hidden" name="id" value="${s.slotId}">
                                <button class="btn btn-sm btn-outline-danger" type="submit" onclick="return confirm('Xóa khung giờ này?');">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty slots}">
                    <tr>
                        <td colspan="5" class="text-center text-muted">Chưa có khung giờ.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
