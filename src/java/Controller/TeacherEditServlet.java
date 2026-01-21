package Controller;

import DAO.TeacherDAO;
import Model.Teacher;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/teachers/edit")
public class TeacherEditServlet extends HttpServlet {
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private static final String TOKEN_KEY = "teacherEditToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            Teacher t = id > 0 ? teacherDAO.findById(id) : null;
            if (t == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            req.setAttribute("teacher", t);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher("/WEB-INF/views/admin/teacher_form.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            int id = parseInt(req.getParameter("teacherId"), -1);
            Teacher existing = id > 0 ? teacherDAO.findById(id) : null;
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            Teacher t = new Teacher();
            t.setTeacherId(id);
            t.setFullName(trim(req.getParameter("fullName")));
            t.setEmail(trimToNull(req.getParameter("email")));
            t.setPhone(trimToNull(req.getParameter("phone")));
            t.setLevel(trimToNull(req.getParameter("level")));
            String status = trim(req.getParameter("status"));
            t.setStatus(status.isEmpty() ? "ACTIVE" : status);

            String validation = validate(t);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("teacher", t);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/teacher_form.jsp").forward(req, resp);
                return;
            }

            teacherDAO.update(t);
            Flash.success(req, "Cập nhật giáo viên thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/teachers");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String validate(Teacher t) {
        if (t.getFullName().isBlank()) return "Vui lòng nhập họ tên.";
        if (t.getFullName().length() > 150) return "Họ tên tối đa 150 ký tự.";
        if (t.getEmail() != null && t.getEmail().length() > 255) return "Email tối đa 255 ký tự.";
        if (t.getPhone() != null && t.getPhone().length() > 30) return "SĐT tối đa 30 ký tự.";
        if (!("ACTIVE".equalsIgnoreCase(t.getStatus()) || "INACTIVE".equalsIgnoreCase(t.getStatus())))
            return "Trạng thái không hợp lệ.";
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

    private static String trimToNull(String s) {
        s = trim(s);
        return s.isEmpty() ? null : s;
    }
}

