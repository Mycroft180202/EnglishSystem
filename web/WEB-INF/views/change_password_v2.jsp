<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Đổi mật khẩu" active="change-password">
    <div class="row justify-content-center">
        <div class="col-12 col-md-8 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h3 class="mb-1">Đổi mật khẩu</h3>
                    <div class="text-muted mb-3">Cập nhật mật khẩu đăng nhập của bạn</div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${empty error && not empty message}">
                        <div class="alert alert-success"><c:out value="${message}"/></div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/app/change-password"
                          onsubmit="this.querySelector('button[type=submit]').disabled=true;">
                        <div class="mb-3">
                            <label class="form-label">Mật khẩu hiện tại</label>
                            <input class="form-control" type="password" name="current" required autocomplete="current-password">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Mật khẩu mới</label>
                            <input class="form-control" type="password" name="next" required autocomplete="new-password" minlength="8">
                            <div class="form-text">Tối thiểu 8 ký tự.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Xác nhận mật khẩu mới</label>
                            <input class="form-control" type="password" name="confirm" required autocomplete="new-password" minlength="8">
                        </div>

                        <div class="d-flex gap-2">
                            <button class="btn btn-primary" type="submit">
                                <i class="bi bi-check2-circle"></i><span class="ms-1">Cập nhật</span>
                            </button>
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/app/home">Quay lại</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</t:layout>
