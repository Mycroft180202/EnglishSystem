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

@WebServlet("/admin/teachers/account")
public class TeacherAccountServlet extends HttpServlet {
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final String TOKEN_KEY = "teacherAccountToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

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

            String username = trim(req.getParameter("username"));
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            String validation = validate(username, password, confirm);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("teacher", t);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/teacher_account.jsp").forward(req, resp);
                return;
            }

            if (userDAO.findByUsername(username) != null) {
                req.setAttribute("error", "Username đã tồn tại.");
                req.setAttribute("teacher", t);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/admin/teacher_account.jsp").forward(req, resp);
                return;
            }

            String hash = PasswordUtil.hashPassword(password.toCharArray());
            userDAO.createTeacherUser(teacherId, username, hash);
            Flash.success(req, "Đã tạo tài khoản giáo viên. Hãy đăng nhập bằng role TEACHER.");
            resp.sendRedirect(req.getContextPath() + "/admin/teachers");
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

