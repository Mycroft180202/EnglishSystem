<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>403 Forbidden</title>
    </head>
    <body>
        <h2>403 - Không có quyền truy cập</h2>
        <p>Bạn không có quyền truy cập chức năng này.</p>
        <p><a href="<%= request.getContextPath() %>/app/home">Về trang chủ</a></p>
    </body>
</html>
