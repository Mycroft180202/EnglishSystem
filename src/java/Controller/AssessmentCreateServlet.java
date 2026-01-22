package Controller;

import DAO.AssessmentDAO;
import DAO.CourseDAO;
import Model.Assessment;
import Model.Course;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/admin/assessments/create")
public class AssessmentCreateServlet extends HttpServlet {
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private static final String TOKEN_KEY = "assessmentCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            List<Course> courses = courseDAO.listAll("ACTIVE");
            req.setAttribute("courses", courses);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.setAttribute("mode", "create");
            req.getRequestDispatcher("/WEB-INF/views/admin/assessment_form.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/assessments");
                return;
            }

            Assessment a = read(req);
            String validation = validate(a);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("assessment", a);
                req.setAttribute("courses", courseDAO.listAll("ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/assessment_form.jsp").forward(req, resp);
                return;
            }

            Assessment conflict = findConflictSameCourseType(a.getCourseId(), a.getType(), -1);
            if (conflict != null) {
                req.setAttribute("error", "Mỗi khóa học chỉ nên có 1 đầu điểm cho mỗi loại (Test 1/Test 2/Final).");
                req.setAttribute("assessment", a);
                req.setAttribute("courses", courseDAO.listAll("ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/assessment_form.jsp").forward(req, resp);
                return;
            }

            try {
                assessmentDAO.create(a);
            } catch (Exception ex) {
                req.setAttribute("error", "Không thể tạo (có thể bị trùng trong cùng khóa học).");
                req.setAttribute("assessment", a);
                req.setAttribute("courses", courseDAO.listAll("ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/assessment_form.jsp").forward(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/assessments?courseId=" + a.getCourseId());
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static Assessment read(HttpServletRequest req) {
        Assessment a = new Assessment();
        a.setCourseId(parseInt(req.getParameter("courseId"), 0));
        a.setName(trim(req.getParameter("name")));
        a.setType(canonicalType(trim(req.getParameter("type"))));
        a.setWeight(weightForType(a.getType()));
        a.setMaxScore(parseDecimal(req.getParameter("maxScore"), BigDecimal.TEN));
        if (a.getName().isBlank()) a.setName(defaultNameForType(a.getType()));
        return a;
    }

    private static String validate(Assessment a) {
        if (a.getCourseId() <= 0) return "Vui lòng chọn khóa học.";
        if (a.getType() == null) return "Type không hợp lệ (chỉ Test 1/Test 2/Final).";
        if (a.getName().isBlank()) return "Vui lòng nhập tên đầu điểm.";
        if (a.getName().length() > 150) return "Tên tối đa 150 ký tự.";
        if (a.getMaxScore() == null || a.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) return "Max score phải > 0.";
        return null;
    }

    private Assessment findConflictSameCourseType(int courseId, String canonicalType, int excludeAssessId) throws Exception {
        for (String t : typeAliases(canonicalType)) {
            Assessment existing = assessmentDAO.findByCourseAndType(courseId, t);
            if (existing != null && existing.getAssessId() != excludeAssessId) return existing;
        }
        return null;
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

    private static String[] typeAliases(String canonicalType) {
        if (canonicalType == null) return new String[0];
        switch (canonicalType) {
            case "TEST1":
                return new String[]{"TEST1", "QUIZ"};
            case "TEST2":
                return new String[]{"TEST2", "MIDTERM"};
            case "FINAL":
                return new String[]{"FINAL"};
            default:
                return new String[0];
        }
    }

    private static String defaultNameForType(String canonicalType) {
        if (canonicalType == null) return "";
        switch (canonicalType) {
            case "TEST1":
                return "Test 1";
            case "TEST2":
                return "Test 2";
            case "FINAL":
                return "Final Test";
            default:
                return "";
        }
    }

    private static BigDecimal weightForType(String canonicalType) {
        if (canonicalType == null) return BigDecimal.ZERO;
        switch (canonicalType) {
            case "TEST1":
                return new BigDecimal("20");
            case "TEST2":
                return new BigDecimal("30");
            case "FINAL":
                return new BigDecimal("40");
            default:
                return BigDecimal.ZERO;
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static BigDecimal parseDecimal(String s, BigDecimal fallback) {
        String t = trim(s).replace(",", "");
        if (t.isEmpty()) return fallback;
        try {
            return new BigDecimal(t);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
