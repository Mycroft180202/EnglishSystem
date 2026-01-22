<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Audit log" active="audit-logs">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <h3 class="m-0">Audit log</h3>
        <span class="text-muted small">Hiển thị 200 bản ghi gần nhất</span>
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
                    <th style="width:72px;">#</th>
                    <th>Thời gian</th>
                    <th>User</th>
                    <th>Action</th>
                    <th class="text-end">Chi tiet</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${logs}" var="l" varStatus="st">
                    <tr>
                        <td class="text-muted"><c:out value="${st.count}"/></td>
                        <td><c:out value="${l.createdAt}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty l.actorUsername}">
                                    <c:out value="${l.actorUsername}"/>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">--</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><span class="badge text-bg-light border"><c:out value="${l.action}"/></span></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/admin/audit-logs/view?id=${l.auditId}">Xem</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty logs}">
                    <tr>
                        <td colspan="5" class="text-center text-muted">Chưa có audit log.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>
