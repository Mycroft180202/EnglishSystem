package Controller;

import DAO.AssessmentDAO;
import DAO.ClassDAO;
import DAO.EnrollmentDAO;
import DAO.ScoreDAO;
import Model.Assessment;
import Model.CenterClass;
import Model.Enrollment;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/teacher/scores")
public class TeacherScoreServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private static final String TOKEN_KEY = "teacherScoreToken";

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

            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null || clazz.getTeacherId() == null || !clazz.getTeacherId().equals(user.getTeacherId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            List<Assessment> assessments = assessmentDAO.listByCourse(clazz.getCourseId());
            int assessId = parseInt(req.getParameter("assessId"), -1);
            if (assessId <= 0 && !assessments.isEmpty()) assessId = assessments.get(0).getAssessId();

            Map<Integer, BigDecimal> scoreMap = assessId > 0 ? scoreDAO.mapScoresByEnrollId(assessId, classId) : Map.of();
            List<Enrollment> enrollments = enrollmentDAO.listActiveByClass(classId);

            req.setAttribute("clazz", clazz);
            req.setAttribute("assessments", assessments);
            req.setAttribute("assessId", assessId);
            req.setAttribute("enrollments", enrollments);
            req.setAttribute("scoreMap", scoreMap);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/teacher/score_entry.jsp").forward(req, resp);
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

            int classId = parseInt(req.getParameter("classId"), -1);
            int assessId = parseInt(req.getParameter("assessId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null || clazz.getTeacherId() == null || !clazz.getTeacherId().equals(user.getTeacherId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/teacher/scores?classId=" + classId + "&assessId=" + assessId);
                return;
            }

            Assessment assessment = assessId > 0 ? assessmentDAO.findById(assessId) : null;
            if (assessment == null || assessment.getCourseId() != clazz.getCourseId()) {
                Flash.error(req, "Đầu điểm không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/teacher/scores?classId=" + classId);
                return;
            }

            List<Enrollment> enrollments = enrollmentDAO.listActiveByClass(classId);
            BigDecimal max = assessment.getMaxScore();
            for (Enrollment e : enrollments) {
                String raw = trim(req.getParameter("score_" + e.getEnrollId()));
                BigDecimal val = parseDecimalOrNull(raw);
                if (val != null) {
                    if (val.compareTo(BigDecimal.ZERO) < 0) val = BigDecimal.ZERO;
                    if (max != null && val.compareTo(max) > 0) val = max;
                }
                scoreDAO.upsertScore(assessId, e.getEnrollId(), val, user.getUserId());
            }

            Flash.success(req, "Đã lưu điểm.");
            resp.sendRedirect(req.getContextPath() + "/teacher/scores?classId=" + classId + "&assessId=" + assessId);
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

    private static BigDecimal parseDecimalOrNull(String s) {
        if (s == null) return null;
        String t = s.trim().replace(",", "");
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

