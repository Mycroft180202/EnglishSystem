package Controller;

import DAO.ClassDAO;
import Model.CenterClass;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/teacher/classes")
public class TeacherClassServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();

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

            List<CenterClass> all = classDAO.listAll(null, null);
            List<CenterClass> mine = all.stream()
                    .filter(c -> c.getTeacherId() != null && c.getTeacherId().equals(user.getTeacherId()))
                    .collect(Collectors.toList());

            req.setAttribute("classes", mine);
            req.getRequestDispatcher("/WEB-INF/views/teacher/class_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
