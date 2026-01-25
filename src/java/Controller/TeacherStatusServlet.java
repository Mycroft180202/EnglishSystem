package Controller;

import DAO.TeacherDAO;
import DAO.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/teachers/status")
public class TeacherStatusServlet extends HttpServlet {
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            String status = trim(req.getParameter("status")).toUpperCase();
            if (id <= 0 || (!"ACTIVE".equals(status) && !"INACTIVE".equals(status))) {
                resp.sendRedirect(req.getContextPath() + "/admin/teachers");
                return;
            }

            teacherDAO.setStatus(id, status);
            userDAO.setStatusByTeacherId(id, status);
            resp.sendRedirect(req.getContextPath() + "/admin/teachers");
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

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
