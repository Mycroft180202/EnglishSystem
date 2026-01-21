package Controller;

import DAO.UserDAO;
import Util.Flash;
import Util.FormToken;
import Util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/users/create")
public class AdminUserCreateServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "adminUserCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.getRequestDispatcher("/WEB-INF/views/admin/user_create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/users/create");
                return;
            }

            String username = trim(req.getParameter("username"));
            String role = trim(req.getParameter("role")).toUpperCase();
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            String validation = validate(username, role, password, confirm);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("username", username);
                req.setAttribute("role", role);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/user_create.jsp").forward(req, resp);
                return;
            }

            if (userDAO.findByUsername(username) != null) {
                req.setAttribute("error", "Username đã tồn tại.");
                req.setAttribute("username", username);
                req.setAttribute("role", role);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/user_create.jsp").forward(req, resp);
                return;
            }

            String hash = PasswordUtil.hashPassword(password.toCharArray());
            userDAO.createUserWithRole(username, hash, role);
            Flash.success(req, "Đã tạo tài khoản " + role + ".");
            resp.sendRedirect(req.getContextPath() + "/app/home");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String validate(String username, String role, String password, String confirm) {
        if (username == null || username.isBlank()) return "Vui lòng nhập username.";
        if (username.length() > 50) return "Username tối đa 50 ký tự.";
        if (!(role.equals("CONSULTANT") || role.equals("ACCOUNTANT"))) return "Role không hợp lệ.";
        if (password == null || password.isBlank()) return "Vui lòng nhập password.";
        if (password.length() < 8) return "Password tối thiểu 8 ký tự.";
        if (!password.equals(confirm)) return "Xác nhận mật khẩu không khớp.";
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

