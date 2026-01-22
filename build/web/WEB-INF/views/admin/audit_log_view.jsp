<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Audit log detail" active="audit-logs">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Audit log detail</h3>
            <div class="text-muted small">#<c:out value="${log.auditId}"/></div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/audit-logs">Back</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <div class="row g-3">
                <div class="col-md-6">
                    <div class="text-muted small">Time</div>
                    <div><c:out value="${log.createdAt}"/></div>
                </div>
                <div class="col-md-6">
                    <div class="text-muted small">User</div>
                    <div>
                        <c:choose>
                            <c:when test="${not empty log.actorUsername}">
                                <c:out value="${log.actorUsername}"/>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">--</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="text-muted small">Action</div>
                    <div><span class="badge text-bg-light border"><c:out value="${log.action}"/></span></div>
                </div>
                <div class="col-md-6">
                    <div class="text-muted small">IP</div>
                    <div><c:out value="${log.ip}"/></div>
                </div>

                <div class="col-md-6">
                    <div class="text-muted small">Entity</div>
                    <div><c:out value="${log.entity}"/></div>
                </div>
                <div class="col-md-6">
                    <div class="text-muted small">Entity ID</div>
                    <div><c:out value="${log.entityId}"/></div>
                </div>

                <div class="col-12">
                    <div class="text-muted small">Data</div>
                    <pre class="mb-0 p-3 border rounded bg-light" style="white-space: pre-wrap;"><c:out value="${log.dataJson}"/></pre>
                </div>
            </div>
        </div>
    </div>
</t:layout>

