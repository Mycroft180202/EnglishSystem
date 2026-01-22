package Controller;

import DAO.ClassDAO;
import DAO.EnrollmentDAO;
import DAO.InvoiceDAO;
import DAO.RoomDAO;
import DAO.StudentDAO;
import Model.CenterClass;
import Model.Room;
import Model.Student;
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

@WebServlet({"/admin/enrollments/create", "/consultant/enrollments/create"})
public class EnrollmentCreateServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private static final String TOKEN_KEY = "enrollCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            loadData(req);
            req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
                return;
            }

            int studentId = parseInt(req.getParameter("studentId"), 0);
            int classId = parseInt(req.getParameter("classId"), 0);
            if (studentId <= 0 || classId <= 0) {
                req.setAttribute("error", "Vui lòng chọn học viên và lớp học.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }

            Student s = studentDAO.findById(studentId);
            CenterClass c = classDAO.findById(classId);
            if (s == null || c == null) {
                req.setAttribute("error", "Dữ liệu không hợp lệ.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            if (!"ACTIVE".equalsIgnoreCase(s.getStatus())) {
                req.setAttribute("error", "Học viên đang INACTIVE.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            if (!"OPEN".equalsIgnoreCase(c.getStatus()) && !"DRAFT".equalsIgnoreCase(c.getStatus())) {
                req.setAttribute("error", "Lớp không ở trạng thái cho phép đăng ký (DRAFT/OPEN).");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }

            int capacity = enrollmentDAO.getClassCapacity(classId);
            int activeCount = enrollmentDAO.countActiveByClass(classId);

            if (c.getRoomId() == null) {
                req.setAttribute("error", "Lớp chưa gán phòng học, không thể đăng ký.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            Room room = roomDAO.findById(c.getRoomId());
            if (room == null) {
                req.setAttribute("error", "Phòng học của lớp không tồn tại, không thể đăng ký.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            if (room.getCapacity() < capacity) {
                req.setAttribute("error", "Sức chứa phòng (" + room.getCapacity() + ") nhỏ hơn sĩ số tối đa của lớp (" + capacity + "). Vui lòng đổi phòng hoặc giảm sĩ số.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            if (room.getCapacity() > 0 && activeCount >= room.getCapacity()) {
                req.setAttribute("error", "Phòng học đã đủ sức chứa.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }
            if (capacity > 0 && activeCount >= capacity) {
                req.setAttribute("error", "Lớp đã đủ sĩ số.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }

            if (enrollmentDAO.hasScheduleConflict(studentId, classId)) {
                req.setAttribute("error", "Học viên bị trùng lịch với lớp khác.");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }

            try {
                int enrollId = enrollmentDAO.create(studentId, classId, "PENDING");
                BigDecimal fee = c.getStandardFee() == null ? BigDecimal.ZERO : c.getStandardFee();
                User actor = SecurityUtil.currentUser(req);
                Integer issuedBy = actor == null ? null : actor.getUserId();
                invoiceDAO.createInvoice(enrollId, fee, BigDecimal.ZERO, issuedBy);
            } catch (Exception ex) {
                req.setAttribute("error", "Không thể đăng ký (có thể đã đăng ký lớp này).");
                req.setAttribute("studentId", studentId);
                req.setAttribute("classId", classId);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                loadData(req);
                req.getRequestDispatcher("/WEB-INF/views/consultant/enrollment_form.jsp").forward(req, resp);
                return;
            }

            Flash.success(req, "Đã tạo đăng ký ở trạng thái chờ thanh toán.");
            resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void loadData(HttpServletRequest req) throws Exception {
        List<Student> students = studentDAO.listActive();
        List<CenterClass> classes = classDAO.listAll(null, null);
        req.setAttribute("students", students);
        req.setAttribute("classes", classes);
    }

    private static String basePath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        return path.startsWith("/admin/") ? "/admin" : "/consultant";
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
