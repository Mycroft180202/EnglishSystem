package Controller;

import DAO.ClassDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/classes/status")
public class ClassStatusServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            String status = trim(req.getParameter("status")).toUpperCase();
            if (id <= 0 || !isValidStatus(status)) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            classDAO.setStatus(id, status);
            resp.sendRedirect(req.getContextPath() + "/admin/classes");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static boolean isValidStatus(String s) {
        return "DRAFT".equals(s) || "OPEN".equals(s) || "CLOSED".equals(s) || "CANCELLED".equals(s);
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

