package Controller;

import DAO.ClassDAO;
import DAO.CourseDAO;
import DAO.RoomDAO;
import DAO.TeacherDAO;
import Model.CenterClass;
import Model.Course;
import Model.Room;
import Model.Teacher;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/admin/classes/create")
public class ClassCreateServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private static final String TOKEN_KEY = "classCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            loadSelectData(req);
            req.setAttribute("mode", "create");
            req.getRequestDispatcher("/WEB-INF/views/admin/class_form.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            CenterClass c = read(req);
            String validation = validate(c);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("clazz", c);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadSelectData(req);
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/class_form.jsp").forward(req, resp);
                return;
            }

            if (c.getRoomId() != null) {
                Room r = roomDAO.findById(c.getRoomId());
                if (r == null) {
                    req.setAttribute("error", "Phòng học không tồn tại.");
                    req.setAttribute("clazz", c);
                    req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                    loadSelectData(req);
                    req.setAttribute("mode", "create");
                    req.getRequestDispatcher("/WEB-INF/views/admin/class_form.jsp").forward(req, resp);
                    return;
                }
                if (r.getCapacity() < c.getCapacity()) {
                    req.setAttribute("error", "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho sĩ số tối đa của lớp (" + c.getCapacity() + ").");
                    req.setAttribute("clazz", c);
                    req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                    loadSelectData(req);
                    req.setAttribute("mode", "create");
                    req.getRequestDispatcher("/WEB-INF/views/admin/class_form.jsp").forward(req, resp);
                    return;
                }
            }
            if (classDAO.findByCode(c.getClassCode()) != null) {
                req.setAttribute("error", "Mã lớp đã tồn tại.");
                req.setAttribute("clazz", c);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadSelectData(req);
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/class_form.jsp").forward(req, resp);
                return;
            }

            classDAO.create(c);
            Flash.success(req, "Tạo lớp học thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/classes");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void loadSelectData(HttpServletRequest req) throws Exception {
        List<Course> courses = courseDAO.listAll("ACTIVE", null);
        List<Teacher> teachers = teacherDAO.listActive();
        List<Room> rooms = roomDAO.listActive();
        req.setAttribute("courses", courses);
        req.setAttribute("teachers", teachers);
        req.setAttribute("rooms", rooms);
    }

    private static CenterClass read(HttpServletRequest req) {
        CenterClass c = new CenterClass();
        c.setCourseId(parseInt(req.getParameter("courseId"), 0));
        c.setClassCode(trim(req.getParameter("classCode")));
        c.setClassName(trim(req.getParameter("className")));
        c.setTeacherId(parseNullableInt(req.getParameter("teacherId")));
        c.setRoomId(parseNullableInt(req.getParameter("roomId")));
        c.setCapacity(parseInt(req.getParameter("capacity"), 0));
        c.setStartDate(parseDate(req.getParameter("startDate")));
        c.setEndDate(parseDate(req.getParameter("endDate")));
        String status = trim(req.getParameter("status"));
        c.setStatus(status.isEmpty() ? "DRAFT" : status.toUpperCase());
        return c;
    }

    private static String validate(CenterClass c) {
        if (c.getCourseId() <= 0) return "Vui lòng chọn khóa học.";
        if (c.getClassCode().isBlank()) return "Vui lòng nhập mã lớp.";
        if (c.getClassCode().length() > 50) return "Mã lớp tối đa 50 ký tự.";
        if (c.getClassName().isBlank()) return "Vui lòng nhập tên lớp.";
        if (c.getClassName().length() > 150) return "Tên lớp tối đa 150 ký tự.";
        if (c.getCapacity() <= 0) return "Sĩ số tối đa phải > 0.";
        if (c.getStartDate() == null) return "Vui lòng chọn ngày bắt đầu.";
        if (c.getEndDate() != null && c.getEndDate().isBefore(c.getStartDate())) return "Ngày kết thúc không hợp lệ.";
        String st = c.getStatus();
        if (!(equalsAny(st, "DRAFT", "OPEN", "CLOSED", "CANCELLED"))) return "Trạng thái không hợp lệ.";
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

    private static Integer parseNullableInt(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static LocalDate parseDate(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
