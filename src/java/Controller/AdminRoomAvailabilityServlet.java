package Controller;

import DAO.ClassDAO;
import DAO.ClassScheduleDAO;
import DAO.ClassScheduleDAO.SlotOccupancy;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/admin/rooms/availability")
public class AdminRoomAvailabilityServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            // Extra guard (AuthzFilter should already protect /admin/*).
            User user = SecurityUtil.currentUser(req);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            int classId = parseInt(req.getParameter("classId"), -1);
            int roomId = parseInt(req.getParameter("roomId"), -1);
            if (classId <= 0 || roomId <= 0) {
                resp.setStatus(400);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"ok\":false,\"error\":\"missing params\"}");
                return;
            }

            ClassDAO.DateRange range = classDAO.resolveDateRange(classId);
            if (range == null) {
                resp.setStatus(400);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"ok\":false,\"error\":\"missing class dates\"}");
                return;
            }

            LocalDate start = range.startDate;
            LocalDate end = range.endDate;
            List<SlotOccupancy> occ = scheduleDAO.listRoomOccupancyForRange(roomId, start, end, classId);

            resp.setStatus(200);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(toJson(occ));
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String toJson(List<SlotOccupancy> occ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"occupied\":[");
        boolean first = true;
        for (SlotOccupancy o : occ) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{')
              .append("\"dayOfWeek\":").append(o.getDayOfWeek()).append(',')
              .append("\"slotId\":").append(o.getSlotId()).append(',')
              .append("\"classId\":").append(o.getClassId()).append(',')
              .append("\"classCode\":").append(jsonStr(o.getClassCode())).append(',')
              .append("\"className\":").append(jsonStr(o.getClassName()))
              .append('}');
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String jsonStr(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"') sb.append('\\').append(c);
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        sb.append('"');
        return sb.toString();
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }
}

