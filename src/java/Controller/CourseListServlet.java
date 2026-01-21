package Controller;

import DAO.CourseDAO;
import Model.Course;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/courses")
public class CourseListServlet extends HttpServlet {
    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);

            String status = trim(req.getParameter("status"));
            if (status.isEmpty()) status = null;

            List<Course> courses = courseDAO.listAll(status);
            req.setAttribute("courses", courses);
            req.setAttribute("status", status);
            req.getRequestDispatcher("/WEB-INF/views/admin/course_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
