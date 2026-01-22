package Controller;

import DAO.UserDAO;
import Model.User;
import Util.Flash;
import Util.PasswordUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/app/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private static final String VIEW = "/WEB-INF/views/change_password_v2.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        User user = SecurityUtil.currentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Flash.consume(req);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            String current = req.getParameter("current");
            String next = req.getParameter("next");
            String confirm = req.getParameter("confirm");

            if (current == null || next == null || confirm == null
                    || current.isBlank() || next.isBlank() || confirm.isBlank()) {
                req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }
            if (!next.equals(confirm)) {
                req.setAttribute("error", "Xác nhận mật khẩu không khớp.");
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }
            if (next.length() < 8) {
                req.setAttribute("error", "Mật khẩu mới tối thiểu 8 ký tự.");
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            User fresh = userDAO.findById(user.getUserId());
            if (fresh == null || !PasswordUtil.verifyPassword(current.toCharArray(), fresh.getPasswordHash())) {
                req.setAttribute("error", "Mật khẩu hiện tại không đúng.");
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            String newHash = PasswordUtil.hashPassword(next.toCharArray());
            userDAO.updatePassword(user.getUserId(), newHash);

            user.setMustChangePassword(false);
            req.getSession(true).setAttribute(SecurityUtil.SESSION_USER, user);

            req.setAttribute("message", "Đổi mật khẩu thành công.");
            req.getRequestDispatcher(VIEW).forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
