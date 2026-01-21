package Controller;

import DAO.AttendanceDAO;
import Model.AttendanceRow;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/teacher/attendance")
public class TeacherAttendanceServlet extends HttpServlet {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private static final String TOKEN_KEY = "attendanceToken";

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

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0 || !attendanceDAO.sessionBelongsToTeacher(sessionId, user.getTeacherId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            List<AttendanceRow> rows = attendanceDAO.listForSession(sessionId);
            req.setAttribute("sessionId", sessionId);
            req.setAttribute("rows", rows);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/teacher/attendance.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getTeacherId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0 || !attendanceDAO.sessionBelongsToTeacher(sessionId, user.getTeacherId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/teacher/attendance?sessionId=" + sessionId);
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

            attendanceDAO.upsertForSession(sessionId, user.getUserId(), updates);
            Flash.success(req, "Đã lưu điểm danh.");
            resp.sendRedirect(req.getContextPath() + "/teacher/attendance?sessionId=" + sessionId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static boolean isValidStatus(String s) {
        return "PRESENT".equals(s) || "ABSENT".equals(s) || "LATE".equals(s) || "EXCUSED".equals(s);
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

