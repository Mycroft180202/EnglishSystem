<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${not empty sessionScope.authUser}">
    <c:redirect url="/app/home"/>
</c:if>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Mycroft English Center</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/app.css" rel="stylesheet">
        <style>
            .hero-bg {
                background: radial-gradient(1200px 600px at 10% 10%, rgba(37,99,235,.22), transparent 55%),
                            radial-gradient(900px 500px at 90% 20%, rgba(16,185,129,.18), transparent 50%),
                            linear-gradient(180deg, #ffffff, #f6f7fb);
                border: 1px solid var(--app-border);
                border-radius: 24px;
            }
            .feature-card { border-radius: 18px; }
        </style>
    </head>
    <body class="app-shell">
        <div class="container py-4 py-lg-5">
            <header class="d-flex align-items-center justify-content-between mb-4">
                <div class="d-flex align-items-center gap-2">
                    <span class="app-brand-dot"></span>
                    <span class="fw-semibold fs-5">Mycroft English Center</span>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/login">
                        <i class="bi bi-box-arrow-in-right"></i><span class="ms-1">Đăng nhập</span>
                    </a>
                    <a class="btn btn-primary" href="<%= request.getContextPath() %>/register">
                        <i class="bi bi-person-plus"></i><span class="ms-1">Đăng ký</span>
                    </a>
                </div>
            </header>

            <section class="hero-bg p-4 p-lg-5 mb-4">
                <div class="row align-items-center g-4">
                    <div class="col-lg-7">
                        <h1 class="fw-bold display-6 mb-3">Học tiếng Anh hiệu quả, lộ trình rõ ràng</h1>
                        <p class="text-muted fs-5 mb-4">
                            IELTS • TOEIC • Giao tiếp • Tiếng Anh nền tảng — lớp nhỏ, lịch linh hoạt, theo dõi tiến độ từng buổi.
                        </p>
                        <div class="d-flex flex-wrap gap-2">
                            <a class="btn btn-primary btn-lg" href="<%= request.getContextPath() %>/register">
                                Bắt đầu ngay
                            </a>
                            <a class="btn btn-outline-secondary btn-lg" href="<%= request.getContextPath() %>/login">
                                Vào hệ thống quản lý
                            </a>
                        </div>
                    </div>
                    <div class="col-lg-5">
                        <div class="card shadow-sm">
                            <div class="card-body p-4">
                                <h5 class="mb-3">Học phí tham khảo</h5>
                                <div class="d-flex justify-content-between mb-2">
                                    <span>Giao tiếp (8 tuần)</span><span class="fw-semibold">2.000.000 VND</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span>TOEIC (10 tuần)</span><span class="fw-semibold">3.500.000 VND</span>
                                </div>
                                <div class="d-flex justify-content-between mb-3">
                                    <span>IELTS (12 tuần)</span><span class="fw-semibold">6.000.000 VND</span>
                                </div>
                                <div class="alert alert-info mb-0">
                                    Học phí thực tế phụ thuộc cấp độ và lớp học. Liên hệ tư vấn để được báo giá chính xác.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="row g-3 mb-4">
                <div class="col-md-4">
                    <div class="card feature-card h-100">
                        <div class="card-body">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <i class="bi bi-calendar2-check fs-4 text-primary"></i>
                                <h5 class="m-0">Lịch học linh hoạt</h5>
                            </div>
                            <p class="text-muted mb-0">Tạo lớp, xếp lịch theo ca học, phòng học, giáo viên — tránh trùng lịch tự động.</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card feature-card h-100">
                        <div class="card-body">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <i class="bi bi-clipboard-data fs-4 text-primary"></i>
                                <h5 class="m-0">Theo dõi kết quả</h5>
                            </div>
                            <p class="text-muted mb-0">Điểm danh theo buổi, nhập điểm theo đầu điểm, học viên xem kết quả mọi lúc.</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card feature-card h-100">
                        <div class="card-body">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <i class="bi bi-receipt fs-4 text-primary"></i>
                                <h5 class="m-0">Học phí minh bạch</h5>
                            </div>
                            <p class="text-muted mb-0">Tạo hóa đơn theo đăng ký, ghi nhận thanh toán, báo cáo doanh thu theo tháng.</p>
                        </div>
                    </div>
                </div>
            </section>

            <footer class="text-center text-muted small py-3">
                © Mycroft English Center
            </footer>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
