package Controller;

import DAO.AssessmentDAO;
import DAO.CourseDAO;
import Model.Assessment;
import Model.Course;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/assessments")
public class AssessmentListServlet extends HttpServlet {
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            int courseId = parseInt(req.getParameter("courseId"), -1);
            List<Course> courses = courseDAO.listAll("ACTIVE", null);
            req.setAttribute("courses", courses);

            if (courseId <= 0 && !courses.isEmpty()) courseId = courses.get(0).getCourseId();
            req.setAttribute("courseId", courseId);

            List<Assessment> assessments = courseId > 0 ? assessmentDAO.listByCourse(courseId) : List.of();
            req.setAttribute("assessments", assessments);
            req.getRequestDispatcher("/WEB-INF/views/admin/assessment_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
