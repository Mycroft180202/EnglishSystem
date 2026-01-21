package Controller;

import DAO.ClassDAO;
import Model.CenterClass;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/classes")
public class ClassListServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            String status = trim(req.getParameter("status"));
            if (status.isEmpty()) status = null;

            List<CenterClass> classes = classDAO.listAll(status);
            req.setAttribute("classes", classes);
            req.setAttribute("status", status);
            req.getRequestDispatcher("/WEB-INF/views/admin/class_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

