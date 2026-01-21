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

@WebServlet("/admin/assessments/edit")
public class AssessmentEditServlet extends HttpServlet {
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private static final String TOKEN_KEY = "assessmentEditToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            Assessment a = id > 0 ? assessmentDAO.findById(id) : null;
            if (a == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/assessments");
                return;
            }

            List<Course> courses = courseDAO.listAll("ACTIVE");
            req.setAttribute("courses", courses);
            req.setAttribute("assessment", a);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.setAttribute("mode", "edit");
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

            int id = parseInt(req.getParameter("assessId"), -1);
            Assessment existing = id > 0 ? assessmentDAO.findById(id) : null;
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/assessments");
                return;
            }

            Assessment a = read(req);
            a.setAssessId(id);
            String validation = validate(a);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("assessment", a);
                req.setAttribute("courses", courseDAO.listAll("ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/assessment_form.jsp").forward(req, resp);
                return;
            }

            try {
                assessmentDAO.update(a);
            } catch (Exception ex) {
                req.setAttribute("error", "Không thể cập nhật (có thể bị trùng tên/type trong cùng khóa).");
                req.setAttribute("assessment", a);
                req.setAttribute("courses", courseDAO.listAll("ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
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
        a.setType(trim(req.getParameter("type")).toUpperCase());
        a.setWeight(parseDecimal(req.getParameter("weight"), BigDecimal.ZERO));
        a.setMaxScore(parseDecimal(req.getParameter("maxScore"), BigDecimal.TEN));
        return a;
    }

    private static String validate(Assessment a) {
        if (a.getCourseId() <= 0) return "Vui lòng chọn khóa học.";
        if (a.getName().isBlank()) return "Vui lòng nhập tên đầu điểm.";
        if (a.getName().length() > 150) return "Tên tối đa 150 ký tự.";
        if (!(equalsAny(a.getType(), "QUIZ", "MIDTERM", "FINAL", "OTHER"))) return "Type không hợp lệ.";
        if (a.getWeight() == null || a.getWeight().compareTo(BigDecimal.ZERO) < 0 || a.getWeight().compareTo(new BigDecimal("100")) > 0)
            return "Weight phải trong [0..100].";
        if (a.getMaxScore() == null || a.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) return "Max score phải > 0.";
        return null;
    }

    private static boolean equalsAny(String s, String... values) {
        if (s == null) return false;
        for (String v : values) if (s.equalsIgnoreCase(v)) return true;
        return false;
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

