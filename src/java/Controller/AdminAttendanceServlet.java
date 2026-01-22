package Controller;

import DAO.AttendanceDAO;
import DAO.ClassSessionDAO;
import Model.AttendanceRow;
import Model.ClassSession;
import Util.Flash;
import Util.FormToken;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/attendance")
public class AdminAttendanceServlet extends HttpServlet {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private static final String TOKEN_KEY = "adminAttendanceToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            List<AttendanceRow> rows = attendanceDAO.listForSession(sessionId);
            req.setAttribute("sessionId", sessionId);
            req.setAttribute("session", session);
            req.setAttribute("rows", rows);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/admin/attendance_v2.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Integer markedByUserId = null;
            try {
                var u = SecurityUtil.currentUser(req);
                if (u != null) markedByUserId = u.getUserId();
            } catch (Exception ignored) {}

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/attendance?sessionId=" + sessionId);
                return;
            }

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            List<AttendanceRow> existingRows = attendanceDAO.listForSession(sessionId);
            Map<Integer, AttendanceRow> updates = new LinkedHashMap<>();
            for (AttendanceRow row : existingRows) {
                String status = trim(req.getParameter("status_" + row.getEnrollId())).toUpperCase();
                if (!isValidStatus(status)) continue;
                String note = trim(req.getParameter("note_" + row.getEnrollId()));
                AttendanceRow upd = new AttendanceRow();
                upd.setEnrollId(row.getEnrollId());
                upd.setStatus(status);
                upd.setNote(note.isEmpty() ? null : note);
                updates.put(upd.getEnrollId(), upd);
            }

            attendanceDAO.upsertForSession(sessionId, markedByUserId, updates);
            Flash.success(req, "Đã lưu điểm danh.");
            resp.sendRedirect(req.getContextPath() + "/admin/attendance?sessionId=" + sessionId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static boolean isValidStatus(String s) {
        return "ATTENDED".equals(s) || "ABSENT".equals(s) || "EXCUSED".equals(s);
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
