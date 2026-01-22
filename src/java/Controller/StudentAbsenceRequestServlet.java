package Controller;

import DAO.AbsenceRequestDAO;
import DAO.AttendanceDAO;
import DAO.ClassSessionDAO;
import DAO.EnrollmentDAO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

@WebServlet("/student/absence-requests/create")
public class StudentAbsenceRequestServlet extends HttpServlet {
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final AbsenceRequestDAO requestDAO = new AbsenceRequestDAO();
    private static final String TOKEN_KEY = "absenceReqToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            Integer enrollId = enrollmentDAO.findEnrollIdForStudentAndSession(user.getStudentId(), sessionId);
            if (enrollId == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("session", session);
            req.setAttribute("sessionId", sessionId);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/student/absence_request.jsp").forward(req, resp);
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
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            if (sessionId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/student/absence-requests/create?sessionId=" + sessionId);
                return;
            }

            ClassSession session = sessionDAO.findByIdExtended(sessionId);
            if (session == null) {
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            Integer enrollId = enrollmentDAO.findEnrollIdForStudentAndSession(user.getStudentId(), sessionId);
            if (enrollId == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            String reason = trim(req.getParameter("reason"));
            if (reason.isEmpty() || reason.length() > 500) {
                Flash.error(req, "Vui lòng nhập lý do (tối đa 500 ký tự).");
                resp.sendRedirect(req.getContextPath() + "/student/absence-requests/create?sessionId=" + sessionId);
                return;
            }

            if (requestDAO.existsForSessionEnroll(sessionId, enrollId)) {
                Flash.error(req, "Bạn đã gửi đơn xin phép cho buổi học này rồi.");
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            int excusedUsed = attendanceDAO.countExcusedByEnroll(enrollId);
            if (excusedUsed >= 3) {
                Flash.error(req, "Bạn đã dùng hết số buổi nghỉ có phép (tối đa 3).");
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            if (session.getSessionDate() == null || session.getStartTime() == null) {
                Flash.error(req, "Buổi học không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            LocalTime start = LocalTime.parse(session.getStartTime());
            LocalDateTime sessionStart = session.getSessionDate().atTime(start);
            if (LocalDateTime.now().isAfter(sessionStart.minusHours(2))) {
                Flash.error(req, "Chỉ được gửi đơn xin phép trước buổi học ít nhất 2 tiếng.");
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            requestDAO.create(sessionId, enrollId, reason, user.getUserId());
            Flash.success(req, "Đã gửi đơn xin phép nghỉ.");
            resp.sendRedirect(req.getContextPath() + "/student/timetable");
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

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

