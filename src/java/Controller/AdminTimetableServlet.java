package Controller;

import DAO.ClassDAO;
import DAO.ClassSessionDAO;
import DAO.TimeSlotDAO;
import Model.CenterClass;
import Model.ClassSession;
import Model.TimeSlot;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/timetable")
public class AdminTimetableServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final TimeSlotDAO slotDAO = new TimeSlotDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null) {
                Flash.error(req, "Vui lòng chọn lớp để xem thời khóa biểu.");
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            LocalDate base = parseDate(req.getParameter("date"));
            if (base == null) base = LocalDate.now();
            LocalDate weekStart = base.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<TimeSlot> slots = slotDAO.listAll();
            List<ClassSession> sessions = sessionDAO.listByClassInRange(classId, weekStart, weekEnd);
            Map<String, ClassSession> sessionMap = new HashMap<>();
            for (ClassSession s : sessions) {
                sessionMap.put(key(s.getSlotId(), s.getSessionDate()), s);
            }

            List<LocalDate> weekDays = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

            req.setAttribute("clazz", clazz);
            req.setAttribute("slots", slots);
            req.setAttribute("weekStart", weekStart);
            req.setAttribute("weekEnd", weekEnd);
            req.setAttribute("weekDays", weekDays);
            req.setAttribute("prevDate", weekStart.minusDays(7));
            req.setAttribute("nextDate", weekStart.plusDays(7));
            req.setAttribute("today", LocalDate.now());
            req.setAttribute("sessionMap", sessionMap);
            req.setAttribute("prevUrl", req.getContextPath() + "/admin/timetable?classId=" + classId + "&date=" + weekStart.minusDays(7));
            req.setAttribute("nextUrl", req.getContextPath() + "/admin/timetable?classId=" + classId + "&date=" + weekStart.plusDays(7));
            req.setAttribute("jumpAction", req.getContextPath() + "/admin/timetable");
            req.setAttribute("attendanceEditBase", req.getContextPath() + "/admin/attendance?sessionId=");
            req.setAttribute("attendanceEditLabel", "Sửa điểm danh");
            req.setAttribute("attendanceEditOnlyToday", false);

            req.getRequestDispatcher("/WEB-INF/views/admin/timetable.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String key(int slotId, LocalDate date) {
        return slotId + "_" + (date == null ? "" : date.toString());
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static LocalDate parseDate(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }
}
