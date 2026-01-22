package Controller;

import DAO.ClassSessionDAO;
import DAO.TimeSlotDAO;
import Model.ClassSession;
import Model.TimeSlot;
import Model.User;
import Util.Flash;
import Util.FormToken;
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

@WebServlet("/teacher/sessions")
public class TeacherSessionServlet extends HttpServlet {
    private final TimeSlotDAO slotDAO = new TimeSlotDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private static final String TOKEN_KEY = "teacherSessionAssessmentToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getTeacherId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            LocalDate base = parseDate(req.getParameter("date"));
            if (base == null) base = LocalDate.now();
            LocalDate weekStart = base.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<TimeSlot> slots = slotDAO.listAll();
            List<ClassSession> weekSessions = sessionDAO.listByTeacherInRange(user.getTeacherId(), weekStart, weekEnd);
            Map<String, ClassSession> sessionMap = new HashMap<>();
            for (ClassSession s : weekSessions) {
                sessionMap.put(key(s.getSlotId(), s.getSessionDate()), s);
            }

            LocalDate today = LocalDate.now();
            List<ClassSession> todaySessions = sessionDAO.listByTeacherInRange(user.getTeacherId(), today, today);

            List<LocalDate> weekDays = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

            req.setAttribute("slots", slots);
            req.setAttribute("weekStart", weekStart);
            req.setAttribute("weekEnd", weekEnd);
            req.setAttribute("weekDays", weekDays);
            req.setAttribute("today", today);
            req.setAttribute("todaySessions", todaySessions);
            req.setAttribute("sessionMap", sessionMap);
            req.setAttribute("prevUrl", req.getContextPath() + "/teacher/sessions?date=" + weekStart.minusDays(7));
            req.setAttribute("nextUrl", req.getContextPath() + "/teacher/sessions?date=" + weekStart.plusDays(7));
            req.setAttribute("jumpAction", req.getContextPath() + "/teacher/sessions");
            req.setAttribute("attendanceEditBase", req.getContextPath() + "/teacher/attendance?sessionId=");
            req.setAttribute("attendanceEditLabel", "Điểm danh");
            req.setAttribute("attendanceEditOnlyToday", true);
            req.setAttribute("sessionAssessBase", req.getContextPath() + "/teacher/session-assessment");
            req.setAttribute("sessionAssessToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/teacher/sessions.jsp").forward(req, resp);
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
