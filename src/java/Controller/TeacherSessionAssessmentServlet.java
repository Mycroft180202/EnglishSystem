package Controller;

import DAO.AssessmentDAO;
import DAO.ClassSessionDAO;
import DAO.SessionAssessmentDAO;
import Model.Assessment;
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

@WebServlet("/teacher/session-assessment")
public class TeacherSessionAssessmentServlet extends HttpServlet {
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final SessionAssessmentDAO sessionAssessmentDAO = new SessionAssessmentDAO();
    private static final String TOKEN_KEY = "teacherSessionAssessmentToken";

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

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/teacher/sessions");
                return;
            }

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            String date = trim(req.getParameter("date"));
            String redirect = req.getContextPath() + "/teacher/sessions" + (date.isEmpty() ? "" : ("?date=" + date));

            ClassSession s = sessionId > 0 ? sessionDAO.findByIdExtended(sessionId) : null;
            if (s == null || s.getTeacherId() != user.getTeacherId()) {
                Flash.error(req, "Buổi học không hợp lệ.");
                resp.sendRedirect(redirect);
                return;
            }

            String action = trim(req.getParameter("action"));
            if (action.equalsIgnoreCase("clear")) {
                sessionAssessmentDAO.clear(sessionId);
                Flash.success(req, "Đã hủy gán kiểm tra cho buổi học này.");
                resp.sendRedirect(redirect);
                return;
            }

            String type = canonicalType(trim(req.getParameter("type")));
            if (type == null) {
                Flash.error(req, "Vui lòng chọn loại kiểm tra (Test 1/Test 2/Final).");
                resp.sendRedirect(redirect);
                return;
            }

            Assessment a = assessmentDAO.findByCourseAndType(s.getCourseId(), type);
            if (a == null) {
                Flash.error(req, "Khóa học chưa có đầu điểm cho " + type + ". Admin cần tạo đầu điểm trước.");
                resp.sendRedirect(redirect);
                return;
            }

            sessionAssessmentDAO.assign(sessionId, s.getClassId(), a.getAssessId(), user.getUserId());
            Flash.success(req, "Đã gán " + type + " cho buổi học #" + s.getSessionNo() + ".");
            resp.sendRedirect(redirect);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String canonicalType(String raw) {
        if (raw == null) return null;
        String t = raw.trim().toUpperCase();
        if (t.isEmpty()) return null;
        if (t.equals("QUIZ")) return "TEST1";
        if (t.equals("MIDTERM")) return "TEST2";
        if (t.equals("TEST1") || t.equals("TEST2") || t.equals("FINAL")) return t;
        return null;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

