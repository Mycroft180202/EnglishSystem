package Controller;

import DAO.TimeSlotDAO;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/time-slots/delete")
public class TimeSlotDeleteServlet extends HttpServlet {
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            if (id <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
                return;
            }

            try {
                timeSlotDAO.delete(id);
                Flash.success(req, "Xóa khung giờ thành công.");
            } catch (Exception ex) {
                Flash.error(req, "Không thể xóa (khung giờ đang được sử dụng).");
            }

            resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
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

