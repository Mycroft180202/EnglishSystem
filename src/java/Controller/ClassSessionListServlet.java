package Controller;

import DAO.ClassDAO;
import DAO.ClassSessionDAO;
import DAO.RoomDAO;
import DAO.TimeSlotDAO;
import Model.CenterClass;
import Model.ClassSession;
import Model.Room;
import Model.TimeSlot;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/class-sessions")
public class ClassSessionListServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null) {
                Flash.error(req, "Vui lòng chọn lớp để xem buổi học.");
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            List<ClassSession> sessions = sessionDAO.listByClass(classId);
            req.setAttribute("clazz", clazz);
            req.setAttribute("sessions", sessions);
            List<TimeSlot> slots = timeSlotDAO.listAll();
            List<Room> rooms = roomDAO.listActive();
            req.setAttribute("slots", slots);
            req.setAttribute("rooms", rooms);
            req.getRequestDispatcher("/WEB-INF/views/admin/class_session_list.jsp").forward(req, resp);
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
