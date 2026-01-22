package Controller;

import DAO.AbsenceRequestDAO;
import DAO.AttendanceDAO;
import DAO.ClassSessionDAO;
import Model.AttendanceRow;
import Model.ClassSession;
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
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/teacher/attendance")
public class TeacherAttendanceServlet extends HttpServlet {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final AbsenceRequestDAO requestDAO = new AbsenceRequestDAO();
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

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }
            if (session.getSessionDate() == null || !LocalDate.now().equals(session.getSessionDate())) {
                Flash.error(req, "Chỉ được phép điểm danh trong ngày.");
                resp.sendRedirect(req.getContextPath() + "/teacher/sessions");
                return;
            }

            List<AttendanceRow> rows = attendanceDAO.listForSession(sessionId);
            req.setAttribute("sessionId", sessionId);
            req.setAttribute("session", session);
            req.setAttribute("rows", rows);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/teacher/attendance_v3.jsp").forward(req, resp);
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

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }
            if (session.getSessionDate() == null || !LocalDate.now().equals(session.getSessionDate())) {
                Flash.error(req, "Chỉ được phép điểm danh trong ngày.");
                resp.sendRedirect(req.getContextPath() + "/teacher/sessions");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/teacher/attendance?sessionId=" + sessionId);
                return;
            }

            String action = trim(req.getParameter("action"));
            if ("teacherCheckIn".equalsIgnoreCase(action)) {
                String err = sessionDAO.markTeacherCheckIn(sessionId, user.getUserId());
                if (err == null) Flash.success(req, "Đã check-in giáo viên.");
                else Flash.error(req, err);
                resp.sendRedirect(req.getContextPath() + "/teacher/attendance?sessionId=" + sessionId);
                return;
            }

            List<AttendanceRow> existingRows = attendanceDAO.listForSession(sessionId);
            Map<Integer, AttendanceRow> updates = new LinkedHashMap<>();
            boolean anyCoerced = false;
            for (AttendanceRow row : existingRows) {
                String status = trim(req.getParameter("status_" + row.getEnrollId())).toUpperCase();
                if (!isValidStatus(status)) continue;
                String note = trim(req.getParameter("note_" + row.getEnrollId()));
                AttendanceRow upd = new AttendanceRow();
                upd.setEnrollId(row.getEnrollId());
                // enforce EXCUSED rules: max 3 + must have request
                if ("EXCUSED".equals(status)) {
                    if (row.getExcusedCount() >= 3) {
                        status = "ABSENT";
                        anyCoerced = true;
                    }
                    if (row.getRequestStatus() == null || row.getRequestStatus().isBlank()) {
                        status = "ABSENT";
                        anyCoerced = true;
                    }
                }
                upd.setStatus(status);
                upd.setNote(note.isEmpty() ? null : note);
                updates.put(upd.getEnrollId(), upd);
            }

            attendanceDAO.upsertForSession(sessionId, user.getUserId(), updates);
            Flash.success(req, "Đã lưu điểm danh.");
            requestDAO.approveAllPendingForSession(sessionId);
            if (anyCoerced) Flash.error(req, "Một số học viên không thể đặt EXCUSED (vượt quá 3 buổi hoặc chưa có đơn xin phép), hệ thống đã chuyển sang ABSENT.");
            resp.sendRedirect(req.getContextPath() + "/teacher/attendance?sessionId=" + sessionId);
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
