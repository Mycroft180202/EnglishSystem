package Controller;

import DAO.ClassSessionDAO;
import DAO.TimeSlotDAO;
import Model.ClassSession;
import Model.TimeSlot;
import Model.User;
import Util.SecurityUtil;
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

@WebServlet("/student/timetable")
public class StudentTimetableServlet extends HttpServlet {
    private final TimeSlotDAO slotDAO = new TimeSlotDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            LocalDate base = parseDate(req.getParameter("date"));
            if (base == null) base = LocalDate.now();
            LocalDate weekStart = base.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<TimeSlot> slots = slotDAO.listAll();
            List<ClassSession> sessions = sessionDAO.listByStudentInRange(user.getStudentId(), weekStart, weekEnd);
            Map<String, ClassSession> sessionMap = new HashMap<>();
            for (ClassSession s : sessions) {
                sessionMap.put(key(s.getSlotId(), s.getSessionDate()), s);
            }

            List<LocalDate> weekDays = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

            req.setAttribute("slots", slots);
            req.setAttribute("weekStart", weekStart);
            req.setAttribute("weekEnd", weekEnd);
            req.setAttribute("weekDays", weekDays);
            req.setAttribute("prevDate", weekStart.minusDays(7));
            req.setAttribute("nextDate", weekStart.plusDays(7));
            req.setAttribute("today", LocalDate.now());
            req.setAttribute("sessionMap", sessionMap);
            req.setAttribute("prevUrl", req.getContextPath() + "/student/timetable?date=" + weekStart.minusDays(7));
            req.setAttribute("nextUrl", req.getContextPath() + "/student/timetable?date=" + weekStart.plusDays(7));
            req.setAttribute("jumpAction", req.getContextPath() + "/student/timetable");
            req.setAttribute("absenceRequestBase", req.getContextPath() + "/student/absence-requests/create?sessionId=");

            req.getRequestDispatcher("/WEB-INF/views/student/timetable.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String key(int slotId, LocalDate date) {
        return slotId + "_" + (date == null ? "" : date.toString());
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
