<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Change Password</title>
    </head>
    <body>
        <h2>Đổi mật khẩu</h2>
        <%
            String error = (String) request.getAttribute("error");
            String message = (String) request.getAttribute("message");
            if (error != null) {
        %>
        <p style="color:red;"><%= error %></p>
        <%
            } else if (message != null) {
        %>
        <p style="color:green;"><%= message %></p>
        <%
            }
        %>
        <form method="post" action="<%= request.getContextPath() %>/app/change-password">
            <div>
                <label>Mật khẩu hiện tại</label>
                <input type="password" name="current" required>
            </div>
            <div>
                <label>Mật khẩu mới</label>
                <input type="password" name="next" required>
            </div>
            <div>
                <label>Xác nhận mật khẩu mới</label>
                <input type="password" name="confirm" required>
            </div>
            <button type="submit">Cập nhật</button>
        </form>
        <p><a href="<%= request.getContextPath() %>/app/home">Về trang chủ</a></p>
    </body>
</html>
