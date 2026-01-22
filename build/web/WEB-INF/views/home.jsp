<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Home" active="home">
    <h3>English Center System</h3>
    <p class="text-muted mb-4">Xin chào, <b><c:out value="${user.username}"/></b></p>

    <c:choose>
        <c:when test="${sessionScope.authUser != null && sessionScope.authUser.roleCodes.contains('ADMIN')}">
            <div class="row g-3">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Khóa học</h5>
                            <p class="card-text">Quản lý danh sách khóa học, học phí chuẩn, trạng thái.</p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/courses">Mở quản lý khóa học</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Giáo viên</h5>
                            <p class="card-text">Quản lý giáo viên, thông tin liên hệ, trình độ.</p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/teachers">Mở quản lý giáo viên</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Phòng học</h5>
                            <p class="card-text">Quản lý phòng học, sức chứa, trạng thái.</p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/rooms">Mở quản lý phòng học</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Lớp học</h5>
                            <p class="card-text">Tạo lớp theo khóa, gán giáo viên/phòng, sĩ số và ngày học.</p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/classes">Mở quản lý lớp học</a>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-info">
                Tài khoản của bạn đã đăng nhập thành công. Các chức năng quản trị chỉ dành cho Admin.
            </div>
        </c:otherwise>
    </c:choose>
</t:layout>
