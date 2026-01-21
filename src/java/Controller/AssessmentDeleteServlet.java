package Controller;

import DAO.AssessmentDAO;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/assessments/delete")
public class AssessmentDeleteServlet extends HttpServlet {
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            int id = parseInt(req.getParameter("id"), -1);
            int courseId = parseInt(req.getParameter("courseId"), -1);
            if (id > 0) {
                try {
                    assessmentDAO.delete(id);
                    Flash.success(req, "Đã xóa đầu điểm.");
                } catch (Exception ex) {
                    Flash.error(req, "Không thể xóa (đầu điểm đang được dùng để nhập điểm).");
                }
            }
            String redirect = req.getContextPath() + "/admin/assessments" + (courseId > 0 ? "?courseId=" + courseId : "");
            resp.sendRedirect(redirect);
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

