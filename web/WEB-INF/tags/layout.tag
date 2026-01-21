<%@tag description="Base layout" pageEncoding="UTF-8"%>
<%@attribute name="title" required="true" rtexprvalue="true" %>
<%@attribute name="active" required="false" rtexprvalue="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:out value="${title}"/></title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/app.css" rel="stylesheet">
    </head>
    <body class="app-shell">
        <nav class="navbar navbar-expand-lg app-topbar">
            <div class="container-fluid">
                <button class="btn btn-outline-secondary d-lg-none me-2" type="button" data-bs-toggle="offcanvas" data-bs-target="#appSidebar">
                    <i class="bi bi-list"></i>
                </button>
                <a class="navbar-brand fw-semibold" href="${pageContext.request.contextPath}/app/home">
                    <span class="app-brand-dot"></span> English Center
                </a>
                <div class="ms-auto d-flex align-items-center gap-2">
                    <span class="text-muted small d-none d-md-inline">
                        <c:out value="${sessionScope.authUser != null ? sessionScope.authUser.username : ''}"/>
                    </span>
                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/app/change-password">
                        <i class="bi bi-shield-lock"></i><span class="ms-1">Đổi mật khẩu</span>
                    </a>
                    <a class="btn btn-sm btn-danger" href="${pageContext.request.contextPath}/logout">
                        <i class="bi bi-box-arrow-right"></i><span class="ms-1">Đăng xuất</span>
                    </a>
                </div>
            </div>
        </nav>

        <div class="offcanvas offcanvas-start app-sidebar d-lg-none" tabindex="-1" id="appSidebar">
            <div class="offcanvas-header">
                <h5 class="offcanvas-title">Menu</h5>
                <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
            </div>
                <div class="offcanvas-body">
                    <jsp:include page="/WEB-INF/views/_sidebar.jsp">
                        <jsp:param name="active" value="${active}"/>
                    </jsp:include>
                </div>
        </div>

        <div class="container-fluid">
            <div class="row">
                <aside class="col-lg-2 d-none d-lg-block app-sidebar p-0">
                    <div class="app-sidebar-inner">
                        <jsp:include page="/WEB-INF/views/_sidebar.jsp">
                            <jsp:param name="active" value="${active}"/>
                        </jsp:include>
                    </div>
                </aside>
                <main class="col-lg-10 app-content">
                    <div class="app-content-inner">
                        <jsp:doBody/>
                    </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
