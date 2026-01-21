package Controller;

import DAO.StudentDAO;
import Model.Student;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet({"/admin/students/create", "/consultant/students/create"})
public class StudentCreateServlet extends HttpServlet {
    private final StudentDAO studentDAO = new StudentDAO();
    private static final String TOKEN_KEY = "studentCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.setAttribute("mode", "create");
        req.getRequestDispatcher("/WEB-INF/views/admin/student_form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/consultant/students");
                return;
            }

            Student s = read(req);
            String validation = validate(s);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("student", s);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/student_form.jsp").forward(req, resp);
                return;
            }

            studentDAO.create(s);
            Flash.success(req, "Tạo học viên thành công.");
            resp.sendRedirect(req.getContextPath() + basePath(req) + "/students");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static Student read(HttpServletRequest req) {
        Student s = new Student();
        s.setFullName(trim(req.getParameter("fullName")));
        s.setDob(parseDate(req.getParameter("dob")));
        s.setGender(trimToNull(req.getParameter("gender")));
        s.setEmail(trimToNull(req.getParameter("email")));
        s.setPhone(trimToNull(req.getParameter("phone")));
        s.setAddress(trimToNull(req.getParameter("address")));
        s.setInputLevel(trimToNull(req.getParameter("inputLevel")));
        String status = trim(req.getParameter("status"));
        s.setStatus(status.isEmpty() ? "ACTIVE" : status.toUpperCase());
        return s;
    }

    private static String validate(Student s) {
        if (s.getFullName().isBlank()) return "Vui lòng nhập họ tên.";
        if (s.getFullName().length() > 150) return "Họ tên tối đa 150 ký tự.";
        if (s.getGender() != null && !(equalsAny(s.getGender(), "M", "F", "O"))) return "Giới tính không hợp lệ.";
        if (s.getEmail() != null && s.getEmail().length() > 255) return "Email tối đa 255 ký tự.";
        if (s.getPhone() != null && s.getPhone().length() > 30) return "SĐT tối đa 30 ký tự.";
        if (s.getStatus() == null || !(equalsAny(s.getStatus(), "ACTIVE", "INACTIVE"))) return "Trạng thái không hợp lệ.";
        return null;
    }

    private static boolean equalsAny(String s, String... values) {
        if (s == null) return false;
        for (String v : values) if (s.equalsIgnoreCase(v)) return true;
        return false;
    }

    private static LocalDate parseDate(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String basePath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        return path.startsWith("/admin/") ? "/admin" : "/consultant";
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trimToNull(String s) {
        s = trim(s);
        return s.isEmpty() ? null : s;
    }
}

