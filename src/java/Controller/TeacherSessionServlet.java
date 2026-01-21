package Controller;

import DAO.ClassSessionDAO;
import Model.ClassSession;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/teacher/sessions")
public class TeacherSessionServlet extends HttpServlet {
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getTeacherId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            List<ClassSession> sessions = sessionDAO.listByTeacher(user.getTeacherId());
            req.setAttribute("sessions", sessions);
            req.setAttribute("teacherId", user.getTeacherId());
            req.getRequestDispatcher("/WEB-INF/views/teacher/session_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
