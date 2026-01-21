package Controller;

import DAO.UserDAO;
import Util.Flash;
import Util.FormToken;
import Util.PasswordUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "registerToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        if (SecurityUtil.currentUser(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/app/home");
            return;
        }

        try {
            if (!userDAO.anyUserExists()) {
                resp.sendRedirect(req.getContextPath() + "/setup");
                return;
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
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

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/register");
                return;
            }

            String username = trim(req.getParameter("username"));
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            String validation = validate(username, password, confirm);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("username", username);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
                return;
            }

            if (userDAO.findByUsername(username) != null) {
                req.setAttribute("error", "Username đã tồn tại.");
                req.setAttribute("username", username);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password.toCharArray());
            userDAO.createUserWithRole(username, passwordHash, "STUDENT");

            Flash.success(req, "Đăng ký tài khoản thành công. Bạn hãy đăng nhập.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String validate(String username, String password, String confirm) {
        if (username == null || username.isBlank()) return "Vui lòng nhập username.";
        if (username.length() > 50) return "Username tối đa 50 ký tự.";
        if (password == null || password.isBlank()) return "Vui lòng nhập password.";
        if (password.length() < 8) return "Password tối thiểu 8 ký tự.";
        if (!password.equals(confirm)) return "Xác nhận mật khẩu không khớp.";
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
