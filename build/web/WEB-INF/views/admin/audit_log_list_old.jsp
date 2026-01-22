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
                    <th>Entity</th>
                    <th>EntityId</th>
                    <th>IP</th>
                    <th>Data</th>
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
                        <td><c:out value="${l.entity}"/></td>
                        <td><c:out value="${l.entityId}"/></td>
                        <td><c:out value="${l.ip}"/></td>
                        <td class="small text-muted" style="max-width: 420px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                            <c:out value="${l.dataJson}"/>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty logs}">
                    <tr>
                        <td colspan="8" class="text-center text-muted">Chưa có audit log.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</t:layout>

<!-- moved to audit_log_list_old.jsp -->
