package Controller;

import DAO.UserDAO;
import Util.Flash;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/app/verify-email")
public class VerifyEmailServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String token = req.getParameter("token");
            Integer ok = userDAO.verifyEmailByToken(token);
            if (ok != null) {
                Flash.success(req, "Xác thực email thành công.");
            } else {
                Flash.error(req, "Link xác thực không hợp lệ hoặc đã hết hạn.");
            }

            String ctx = req.getContextPath();
            if (SecurityUtil.currentUser(req) != null) {
                resp.sendRedirect(ctx + "/app/profile");
            } else {
                resp.sendRedirect(ctx + "/login");
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
