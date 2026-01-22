package Controller;

import DAO.UserDAO;
import Model.User;
import Util.Flash;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (SecurityUtil.currentUser(req) != null) {
                resp.sendRedirect(req.getContextPath() + "/app/home");
                return;
            }

            Flash.consume(req);
            req.setAttribute("setupAvailable", !userDAO.anyUserExists());
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!userDAO.anyUserExists()) {
                resp.sendRedirect(req.getContextPath() + "/setup");
                return;
            }

            req.setAttribute("setupAvailable", false);

            String username = trim(req.getParameter("username"));
            String password = req.getParameter("password");

            if (username.isEmpty() || password == null || password.isBlank()) {
                req.setAttribute("error", "Vui lòng nhập username và password.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            User existing = userDAO.findByUsername(username);
            if (existing == null) {
                req.setAttribute("error", "Chưa có đăng ký tài khoản này.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            User user = userDAO.authenticate(username, password.toCharArray());
            if (user == null) {
                req.setAttribute("error", "Sai mật khẩu.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute(SecurityUtil.SESSION_USER, user);

            if (user.isMustChangePassword()) {
                Flash.success(req, "Vui lòng đổi mật khẩu trước khi sử dụng hệ thống.");
                resp.sendRedirect(req.getContextPath() + "/app/change-password");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/app/home");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
