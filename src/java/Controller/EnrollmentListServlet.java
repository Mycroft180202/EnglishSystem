package Controller;

import DAO.EnrollmentDAO;
import Model.Enrollment;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({"/admin/enrollments", "/consultant/enrollments"})
public class EnrollmentListServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            Integer classId = parseNullableInt(req.getParameter("classId"));
            Integer studentId = parseNullableInt(req.getParameter("studentId"));
            String status = trim(req.getParameter("status"));
            if (status.isEmpty()) status = null;

            List<Enrollment> enrollments = enrollmentDAO.listAll(classId, studentId, status);
            req.setAttribute("enrollments", enrollments);
            req.setAttribute("status", status);
            req.setAttribute("classId", classId);
            req.setAttribute("studentId", studentId);
            req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static Integer parseNullableInt(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

