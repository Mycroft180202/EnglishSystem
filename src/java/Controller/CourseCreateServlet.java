package Controller;

import DAO.CourseDAO;
import Model.Course;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@WebServlet("/admin/courses/create")
public class CourseCreateServlet extends HttpServlet {
    private final CourseDAO courseDAO = new CourseDAO();
    private static final String TOKEN_KEY = "courseCreateToken";
    private static final Map<String, Integer> CEFR_RANK = Map.of(
            "A1", 1,
            "A2", 2,
            "B1", 3,
            "B2", 4,
            "C1", 5,
            "C2", 6
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.setAttribute("levelFrom", "");
        req.setAttribute("levelTo", "");
        req.setAttribute("mode", "create");
        req.getRequestDispatcher("/WEB-INF/views/admin/course_form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String providedToken = req.getParameter("formToken");
            if (!FormToken.consume(req, TOKEN_KEY, providedToken)) {
                resp.sendRedirect(req.getContextPath() + "/admin/courses");
                return;
            }

            applyLevelAttrsFromRequest(req);

            Course c = readCourseFromRequest(req);
            String validation = validate(c, true);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("course", c);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/course_form.jsp").forward(req, resp);
                return;
            }

            if (courseDAO.findByCode(c.getCourseCode()) != null) {
                req.setAttribute("error", "Mã khóa học đã tồn tại.");
                req.setAttribute("course", c);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/course_form.jsp").forward(req, resp);
                return;
            }

            courseDAO.create(c);
            Flash.success(req, "Tạo khóa học thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/courses");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static Course readCourseFromRequest(HttpServletRequest req) {
        Course c = new Course();
        c.setCourseCode(trim(req.getParameter("courseCode")));
        c.setCourseName(trim(req.getParameter("courseName")));
        c.setDescription(trimToNull(req.getParameter("description")));
        String levelFrom = trimToNull(req.getParameter("levelFrom"));
        String levelTo = trimToNull(req.getParameter("levelTo"));
        if (levelFrom == null && levelTo == null) {
            String legacy = trimToNull(req.getParameter("level"));
            c.setLevel(legacy);
        } else if (levelFrom != null && levelTo != null) {
            c.setLevel(levelFrom + "-" + levelTo);
        } else {
            c.setLevel("INVALID_RANGE");
        }
        c.setDurationWeeks(parseInt(req.getParameter("durationWeeks"), 0));
        c.setStandardFee(parseMoney(req.getParameter("standardFee")));
        String status = trim(req.getParameter("status"));
        c.setStatus(status.isEmpty() ? "ACTIVE" : status);
        return c;
    }

    private static String validate(Course c, boolean creating) {
        if (c.getCourseCode() == null || c.getCourseCode().isBlank()) return "Vui lòng nhập mã khóa học.";
        if (c.getCourseCode().length() > 50) return "Mã khóa học tối đa 50 ký tự.";
        if (c.getCourseName() == null || c.getCourseName().isBlank()) return "Vui lòng nhập tên khóa học.";
        if (c.getCourseName().length() > 150) return "Tên khóa học tối đa 150 ký tự.";
        String levelValidation = validateLevelRangeAllowLegacy(c.getLevel());
        if (levelValidation != null) return levelValidation;
        if (c.getDurationWeeks() <= 0) return "Thời lượng (tuần) phải > 0.";
        if (c.getStandardFee() != null && c.getStandardFee().compareTo(BigDecimal.ZERO) < 0) return "Học phí không hợp lệ.";
        String st = c.getStatus();
        if (!(equalsAny(st, "ACTIVE", "INACTIVE"))) return "Trạng thái không hợp lệ.";
        return null;
    }

    private static void applyLevelAttrsFromRequest(HttpServletRequest req) {
        req.setAttribute("levelFrom", trim(req.getParameter("levelFrom")));
        req.setAttribute("levelTo", trim(req.getParameter("levelTo")));
    }

    private static String validateLevelRangeAllowLegacy(String level) {
        if (level == null || level.isBlank()) return null;
        if ("INVALID_RANGE".equals(level)) return "Trình độ yêu cầu không hợp lệ. Hãy chọn đầy đủ Từ... và Đến... theo CEFR.";
        String[] range = parseLevelRange(level);
        if (range == null) return null;
        Integer a = CEFR_RANK.get(range[0]);
        Integer b = CEFR_RANK.get(range[1]);
        if (a == null || b == null) return "Trình độ yêu cầu không hợp lệ. Hãy chọn theo CEFR A1–C2.";
        if (a > b) return "Trình độ yêu cầu không hợp lệ: mức 'Từ' phải nhỏ hơn hoặc bằng mức 'Đến'.";
        return null;
    }

    private static String[] parseLevelRange(String level) {
        if (level == null) return null;
        String t = level.trim().toUpperCase();
        if (t.isEmpty()) return null;
        t = t.replace("–", "-").replace("—", "-");
        int dash = t.indexOf('-');
        if (dash < 0) return null;
        String a = t.substring(0, dash).trim();
        String b = t.substring(dash + 1).trim();
        if (a.isEmpty() || b.isEmpty()) return null;
        if (!CEFR_RANK.containsKey(a) || !CEFR_RANK.containsKey(b)) return null;
        return new String[] { a, b };
    }

    private static boolean equalsAny(String s, String... values) {
        if (s == null) return false;
        for (String v : values) if (s.equalsIgnoreCase(v)) return true;
        return false;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trimToNull(String s) {
        s = trim(s);
        return s.isEmpty() ? null : s;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(trim(s));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static BigDecimal parseMoney(String s) {
        String t = trim(s);
        if (t.isEmpty()) return BigDecimal.ZERO;
        t = t.replace(",", "");
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
