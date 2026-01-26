<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Thông tin cá nhân" active="profile">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Thông tin cá nhân</h3>
            <div class="text-muted small">Cập nhật email, số điện thoại, địa chỉ và đổi mật khẩu.</div>
        </div>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success"><c:out value="${flashSuccess}"/></div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger"><c:out value="${flashError}"/></div>
    </c:if>

    <div class="row g-3">
        <div class="col-12 col-lg-7">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    <div class="fw-semibold">Thông tin</div>
                </div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/app/profile" class="vstack gap-3">
                        <input type="hidden" name="action" value="updateInfo"/>
                        <input type="hidden" name="infoToken" value="${infoToken}"/>

                        <div>
                            <label class="form-label">Họ tên đầy đủ</label>
                            <input class="form-control" name="fullName" required maxlength="150" value="<c:out value='${u.fullName}'/>"/>
                        </div>

                        <div class="row g-2">
                            <div class="col-12 col-md-6">
                                <label class="form-label">Email</label>
                                <input class="form-control" name="email" type="email" maxlength="255" value="<c:out value='${u.email}'/>"/>
                            </div>
                            <div class="col-12 col-md-6">
                                <label class="form-label">Số điện thoại</label>
                                <input class="form-control" name="phone" maxlength="30" value="<c:out value='${u.phone}'/>"/>
                            </div>
                        </div>

                        <div>
                            <label class="form-label">Địa chỉ <span class="text-muted">(không bắt buộc)</span></label>
                            <input class="form-control" name="address" maxlength="255" value="<c:out value='${u.address}'/>"/>
                        </div>

                        <div class="d-flex justify-content-end">
                            <button class="btn btn-primary" type="submit">
                                <i class="bi bi-save"></i><span class="ms-1">Lưu thay đổi</span>
                            </button>
                        </div>
                    </form>

                    <hr class="my-4"/>

                    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between">
                        <div>
                            <div class="fw-semibold">Xác thực email</div>
                            <div class="small text-muted">
                                <c:choose>
                                    <c:when test="${empty u.email}">Bạn chưa có email.</c:when>
                                    <c:when test="${u.emailVerified}">Email đã được xác thực.</c:when>
                                    <c:otherwise>Email chưa được xác thực.</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <c:if test="${not empty u.email}">
                                <c:choose>
                                    <c:when test="${u.emailVerified}">
                                        <span class="badge text-bg-success">Đã xác thực</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge text-bg-secondary">Chưa xác thực</span>
                                        <form method="post" action="${pageContext.request.contextPath}/app/profile" class="m-0">
                                            <input type="hidden" name="action" value="sendVerify"/>
                                            <input type="hidden" name="emailToken" value="${emailToken}"/>
                                            <button class="btn btn-outline-primary btn-sm" type="submit">
                                                <i class="bi bi-envelope-check"></i><span class="ms-1">Gửi email xác thực</span>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </div>
                    <div class="small text-muted mt-2">
                        Lưu ý: bản demo hiện tại sẽ in link xác thực vào log server.
                    </div>
                </div>
            </div>
        </div>

        <div class="col-12 col-lg-5">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    <div class="fw-semibold">Đổi mật khẩu</div>
                </div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/app/profile" class="vstack gap-3">
                        <input type="hidden" name="action" value="changePassword"/>
                        <input type="hidden" name="pwdToken" value="${pwdToken}"/>

                        <div>
                            <label class="form-label">Mật khẩu hiện tại</label>
                            <input class="form-control" type="password" name="currentPassword" required/>
                        </div>
                        <div>
                            <label class="form-label">Mật khẩu mới</label>
                            <input class="form-control" type="password" name="newPassword" minlength="8" required/>
                            <div class="form-text">Tối thiểu 8 ký tự.</div>
                        </div>
                        <div>
                            <label class="form-label">Xác nhận mật khẩu mới</label>
                            <input class="form-control" type="password" name="confirmPassword" minlength="8" required/>
                        </div>

                        <div class="d-flex justify-content-end">
                            <button class="btn btn-outline-primary" type="submit">
                                <i class="bi bi-shield-lock"></i><span class="ms-1">Đổi mật khẩu</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</t:layout>
