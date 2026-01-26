<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="p-3">
    <div class="app-nav-section">
        <a class="app-nav-link ${param.active == 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/home">
            <i class="bi bi-speedometer2"></i><span>Dashboard</span>
        </a>
        <a class="app-nav-link ${param.active == 'profile' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/profile">
            <i class="bi bi-person"></i><span>Thông tin cá nhân</span>
        </a>
        <a class="app-nav-link ${param.active == 'change-password' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/change-password">
            <i class="bi bi-shield-lock"></i><span>Đổi mật khẩu</span>
        </a>
    </div>

    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.roleCodes.contains('ADMIN')}">
        <div class="app-nav-title">Admin</div>
        <div class="app-nav-section">
            <a class="app-nav-link ${param.active == 'audit-logs' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/audit-logs">
                <i class="bi bi-journal-text"></i><span>Audit log</span>
            </a>
            <a class="app-nav-link ${param.active == 'courses' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/courses">
                <i class="bi bi-journal-bookmark"></i><span>Khóa học</span>
            </a>
            <a class="app-nav-link ${param.active == 'classes' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/classes">
                <i class="bi bi-people"></i><span>Lớp học</span>
            </a>
            <a class="app-nav-link ${param.active == 'time-slots' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/time-slots">
                <i class="bi bi-clock"></i><span>Khung giờ</span>
            </a>
            <a class="app-nav-link ${param.active == 'rooms' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/rooms">
                <i class="bi bi-door-open"></i><span>Phòng học</span>
            </a>
            <a class="app-nav-link ${param.active == 'teachers' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/teachers">
                <i class="bi bi-mortarboard"></i><span>Giáo viên</span>
            </a>
            <a class="app-nav-link ${param.active == 'assessments' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/assessments">
                <i class="bi bi-ui-checks"></i><span>Đầu điểm</span>
            </a>
            <a class="app-nav-link ${param.active == 'admin-users' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users/create">
                <i class="bi bi-person-plus"></i><span>Tạo user</span>
            </a>
        </div>
    </c:if>

    <c:if test="${sessionScope.authUser != null && (sessionScope.authUser.roleCodes.contains('ADMIN') || sessionScope.authUser.roleCodes.contains('CONSULTANT'))}">
        <div class="app-nav-title">Tư vấn</div>
        <div class="app-nav-section">
            <a class="app-nav-link ${param.active == 'students' ? 'active' : ''}" href="${pageContext.request.contextPath}/consultant/students">
                <i class="bi bi-person-badge"></i><span>Học viên</span>
            </a>
            <a class="app-nav-link ${param.active == 'enrollments' ? 'active' : ''}" href="${pageContext.request.contextPath}/consultant/enrollments">
                <i class="bi bi-clipboard-check"></i><span>Đăng ký học</span>
            </a>
            <a class="app-nav-link ${param.active == 'wallet-withdrawals' ? 'active' : ''}" href="${pageContext.request.contextPath}/consultant/wallet-withdrawals">
                <i class="bi bi-cash-stack"></i><span>Rút tiền ví</span>
            </a>
        </div>
    </c:if>

    <c:if test="${sessionScope.authUser != null && (sessionScope.authUser.roleCodes.contains('ADMIN') || sessionScope.authUser.roleCodes.contains('ACCOUNTANT'))}">
        <div class="app-nav-title">Kế toán</div>
        <div class="app-nav-section">
            <a class="app-nav-link ${param.active == 'invoices' ? 'active' : ''}" href="${pageContext.request.contextPath}/accounting/invoices">
                <i class="bi bi-receipt"></i><span>Hóa đơn</span>
            </a>
            <a class="app-nav-link ${param.active == 'payment-requests' ? 'active' : ''}" href="${pageContext.request.contextPath}/accounting/payment-requests">
                <i class="bi bi-bell"></i><span>Yêu cầu thu tiền</span>
            </a>
            <a class="app-nav-link ${param.active == 'wallet-withdrawals' ? 'active' : ''}" href="${pageContext.request.contextPath}/accounting/wallet-withdrawals">
                <i class="bi bi-cash-stack"></i><span>Yêu cầu rút tiền</span>
            </a>
            <a class="app-nav-link ${param.active == 'revenue-report' ? 'active' : ''}" href="${pageContext.request.contextPath}/accounting/reports/revenue">
                <i class="bi bi-graph-up-arrow"></i><span>Doanh thu</span>
            </a>
        </div>
    </c:if>

    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.roleCodes.contains('TEACHER')}">
        <div class="app-nav-title">Giáo viên</div>
        <div class="app-nav-section">
            <a class="app-nav-link ${param.active == 'teacher-classes' ? 'active' : ''}" href="${pageContext.request.contextPath}/teacher/classes">
                <i class="bi bi-collection"></i><span>Lớp của tôi</span>
            </a>
            <a class="app-nav-link ${param.active == 'teacher-sessions' ? 'active' : ''}" href="${pageContext.request.contextPath}/teacher/sessions">
                <i class="bi bi-calendar2-week"></i><span>Buổi dạy</span>
            </a>
        </div>
    </c:if>

    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.roleCodes.contains('STUDENT')}">
        <div class="app-nav-title">Học viên</div>
        <div class="app-nav-section">
            <a class="app-nav-link ${param.active == 'student-classes' ? 'active' : ''}" href="${pageContext.request.contextPath}/student/classes">
                <i class="bi bi-ui-checks"></i><span>Đăng ký học</span>
            </a>
            <a class="app-nav-link ${param.active == 'student-wallet' ? 'active' : ''}" href="${pageContext.request.contextPath}/student/wallet">
                <i class="bi bi-wallet2"></i><span>Ví</span>
            </a>
            <a class="app-nav-link ${param.active == 'student-timetable' ? 'active' : ''}" href="${pageContext.request.contextPath}/student/timetable">
                <i class="bi bi-calendar3"></i><span>Lịch học</span>
            </a>
            <a class="app-nav-link ${param.active == 'student-results' ? 'active' : ''}" href="${pageContext.request.contextPath}/student/results">
                <i class="bi bi-bar-chart"></i><span>Kết quả</span>
            </a>
            <a class="app-nav-link ${param.active == 'student-fees' ? 'active' : ''}" href="${pageContext.request.contextPath}/student/fees">
                <i class="bi bi-cash-coin"></i><span>Học phí</span>
            </a>
        </div>
    </c:if>
</div>
