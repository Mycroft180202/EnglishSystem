package Controller;

import DAO.StudentDAO;
import DAO.UserDAO;
import Model.Student;
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

@WebServlet({"/admin/students/account", "/consultant/students/account"})
public class StudentAccountServlet extends HttpServlet {
    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "studentAccountToken";
    private static final SecureRandom RNG = new SecureRandom();
    private static final String DEFAULT_PASSWORD = "12345678";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            int studentId = parseInt(req.getParameter("studentId"), -1);
            Student s = studentId > 0 ? studentDAO.findById(studentId) : null;
            if (s == null) {
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/students");
                return;
            }

            User existing = userDAO.findStudentAccount(studentId);
            req.setAttribute("student", s);
            req.setAttribute("account", existing);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/admin/student_account.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int studentId = parseInt(req.getParameter("studentId"), -1);
            if (studentId <= 0) {
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/students");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/students/account?studentId=" + studentId);
                return;
            }

            Student s = studentDAO.findById(studentId);
            if (s == null) {
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/students");
                return;
            }

            if (userDAO.findStudentAccount(studentId) != null) {
                Flash.error(req, "Học viên này đã có tài khoản.");
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/students/account?studentId=" + studentId);
                return;
            }

            String username = generateUniqueUsername("student", 6);
            String hash = PasswordUtil.hashPassword(DEFAULT_PASSWORD.toCharArray());
            userDAO.createStudentUser(studentId, username, hash);
            Flash.success(req, "Đã tạo tài khoản: " + username + " / " + DEFAULT_PASSWORD + ". Bắt buộc đổi mật khẩu khi đăng nhập lần đầu.");
            resp.sendRedirect(req.getContextPath() + basePath(req) + "/students/account?studentId=" + studentId);
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

    private static String basePath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        return path.startsWith("/admin/") ? "/admin" : "/consultant";
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
