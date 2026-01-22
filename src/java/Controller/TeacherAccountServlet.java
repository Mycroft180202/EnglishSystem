package Controller;

import DAO.TeacherDAO;
import DAO.UserDAO;
import Model.Teacher;
import Model.User;
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

@WebServlet("/admin/teachers/account")
public class TeacherAccountServlet extends HttpServlet {
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "teacherAccountToken";
    private static final SecureRandom RNG = new SecureRandom();
    private static final String DEFAULT_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            int teacherId = parseInt(req.getParameter("teacherId"), -1);
            Teacher t = teacherId > 0 ? teacherDAO.findById(teacherId) : null;
            if (t == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            User existing = userDAO.findTeacherAccount(teacherId);
            req.setAttribute("teacher", t);
            req.setAttribute("account", existing);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/admin/teacher_account.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int teacherId = parseInt(req.getParameter("teacherId"), -1);
            if (teacherId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers/account?teacherId=" + teacherId);
                return;
            }

            Teacher t = teacherDAO.findById(teacherId);
            if (t == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            if (userDAO.findTeacherAccount(teacherId) != null) {
                Flash.error(req, "Giáo viên này đã có tài khoản.");
                resp.sendRedirect(req.getContextPath() + "/admin/teachers/account?teacherId=" + teacherId);
                return;
            }

            String username = generateUniqueUsername("teacher", 4);
            String hash = PasswordUtil.hashPassword(DEFAULT_PASSWORD.toCharArray());
            userDAO.createTeacherUser(teacherId, username, hash);

            Flash.success(req, "Đã tạo tài khoản: " + username + " / " + DEFAULT_PASSWORD + ". Bắt buộc đổi mật khẩu khi đăng nhập lần đầu.");
            resp.sendRedirect(req.getContextPath() + "/admin/teachers/account?teacherId=" + teacherId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
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

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
