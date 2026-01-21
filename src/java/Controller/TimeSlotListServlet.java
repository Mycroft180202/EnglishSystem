package Controller;

import DAO.TimeSlotDAO;
import Model.TimeSlot;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/time-slots")
public class TimeSlotListServlet extends HttpServlet {
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            List<TimeSlot> slots = timeSlotDAO.listAll();
            req.setAttribute("slots", slots);
            req.getRequestDispatcher("/WEB-INF/views/admin/time_slot_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}

