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
import java.security.SecureRandom;

@WebServlet("/admin/users/create")
public class AdminUserCreateServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "adminUserCreateToken";
    private static final SecureRandom RNG = new SecureRandom();
    private static final String DEFAULT_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Flash.consume(req);
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

            String role = trim(req.getParameter("role")).toUpperCase();

            String validation = validate(role);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("role", role);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/user_create.jsp").forward(req, resp);
                return;
            }

            String prefix = role.equals("CONSULTANT") ? "consultant" : "accountant";
            String username = generateUniqueUsername(prefix, 4);
            String hash = PasswordUtil.hashPassword(DEFAULT_PASSWORD.toCharArray());
            userDAO.createUserWithRole(username, hash, role, true);

            Flash.success(req, "Đã tạo tài khoản: " + username + " / " + DEFAULT_PASSWORD + " (" + role + "). Bắt buộc đổi mật khẩu khi đăng nhập lần đầu.");
            resp.sendRedirect(req.getContextPath() + "/admin/users/create?role=" + role);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String validate(String role) {
        if (!(role.equals("CONSULTANT") || role.equals("ACCOUNTANT"))) return "Role không hợp lệ.";
        return null;
    }

    private String generateUniqueUsername(String prefix, int digits) throws Exception {
        for (int attempt = 0; attempt < 1000; attempt++) {
            String username = prefix + randomDigits(digits);
            if (userDAO.findByUsername(username) == null) return username;
        }
        throw new IllegalStateException("Unable to generate unique username");
    }

    private static String randomDigits(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
