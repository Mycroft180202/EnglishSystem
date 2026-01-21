<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Đăng nhập</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/app.css" rel="stylesheet">
    </head>
    <body class="app-shell">
        <div class="container py-5" style="max-width: 520px;">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <a class="text-decoration-none fw-semibold" href="${pageContext.request.contextPath}/">
                    <span class="app-brand-dot"></span> English Center
                </a>
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/">
                    <i class="bi bi-arrow-left"></i><span class="ms-1">Landing</span>
                </a>
            </div>

            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h3 class="mb-1">Đăng nhập</h3>
                    <div class="text-muted mb-3">Vào hệ thống quản lý trung tâm</div>

                    <c:if test="${not empty flashSuccess}">
                        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
                    </c:if>
                    <c:if test="${not empty flashError}">
                        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}"/></div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/login"
                          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
                        <div class="mb-3">
                            <label class="form-label">Username</label>
                            <input class="form-control" type="text" name="username" required autofocus>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Password</label>
                            <input class="form-control" type="password" name="password" required>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">
                            <i class="bi bi-box-arrow-in-right"></i><span class="ms-1">Đăng nhập</span>
                        </button>
                    </form>

                    <div class="d-flex justify-content-between mt-3">
                        <a href="${pageContext.request.contextPath}/register">Đăng ký học viên</a>
                        <c:if test="${setupAvailable}">
                            <a href="${pageContext.request.contextPath}/setup">Khởi tạo Admin</a>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
