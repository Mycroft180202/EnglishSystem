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

@WebServlet({"/admin/students/account", "/consultant/students/account"})
public class StudentAccountServlet extends HttpServlet {
    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "studentAccountToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

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

            String username = trim(req.getParameter("username"));
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            String validation = validate(username, password, confirm);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("student", s);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/student_account.jsp").forward(req, resp);
                return;
            }

            if (userDAO.findByUsername(username) != null) {
                req.setAttribute("error", "Username đã tồn tại.");
                req.setAttribute("student", s);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/student_account.jsp").forward(req, resp);
                return;
            }

            String hash = PasswordUtil.hashPassword(password.toCharArray());
            userDAO.createStudentUser(studentId, username, hash);
            Flash.success(req, "Đã tạo tài khoản học viên. Học viên đăng nhập bằng role STUDENT.");
            resp.sendRedirect(req.getContextPath() + basePath(req) + "/students");
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

