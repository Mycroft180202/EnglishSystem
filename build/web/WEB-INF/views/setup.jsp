<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Initial Setup</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/app.css" rel="stylesheet">
    </head>
    <body class="app-shell">
        <div class="container py-5" style="max-width: 560px;">
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
        <h2>Tạo Admin ban đầu</h2>
        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
        <div class="alert alert-danger"><%= error %></div>
        <%
            }
        %>
        <form class="mt-3" method="post" action="<%= request.getContextPath() %>/setup"
              onsubmit="this.querySelector('button[type=submit]').disabled=true;">
            <div class="mb-3">
                <label>Admin username</label>
                <input class="form-control" type="text" name="username" required autofocus>
            </div>
            <div class="mb-3">
                <label>Password</label>
                <input class="form-control" type="password" name="password" required>
            </div>
            <div class="mb-3">
                <label>Confirm password</label>
                <input class="form-control" type="password" name="confirm" required>
            </div>
            <button class="btn btn-primary w-100" type="submit">Create admin</button>
        </form>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
