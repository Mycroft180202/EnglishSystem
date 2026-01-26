<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Đăng ký</title>
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
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/login">
                    <i class="bi bi-box-arrow-in-right"></i><span class="ms-1">Đăng nhập</span>
                </a>
            </div>
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h3 class="mb-3">Đăng ký tài khoản (Học viên)</h3>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}"/></div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/register"
                          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
                        <input type="hidden" name="formToken" value="${formToken}">

                        <div class="mb-3">
                            <label class="form-label">Username</label>
                            <input class="form-control" name="username" maxlength="50" required value="${username}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Họ tên đầy đủ</label>
                            <input class="form-control" name="fullName" maxlength="150" required value="${fullName}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email (không bắt buộc)</label>
                            <input class="form-control" type="email" name="email" maxlength="255" value="${email}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Số điện thoại (không bắt buộc)</label>
                            <input class="form-control" name="phone" maxlength="30" value="${phone}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Password</label>
                            <input class="form-control" type="password" name="password" required>
                            <div class="form-text">Tối thiểu 8 ký tự.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Confirm password</label>
                            <input class="form-control" type="password" name="confirm" required>
                        </div>

                        <button class="btn btn-primary w-100" type="submit">Tạo tài khoản</button>
                    </form>

                    <div class="mt-3 d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/">Landing</a>
                        <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
