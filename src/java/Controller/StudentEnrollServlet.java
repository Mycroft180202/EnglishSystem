package Controller;

import DAO.ClassDAO;
import DAO.ClassScheduleDAO;
import DAO.EnrollmentDAO;
import DAO.InvoiceDAO;
import DAO.PaymentDAO;
import DAO.WalletDAO;
import Model.CenterClass;
import Model.ClassSchedule;
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

@WebServlet("/student/enroll")
public class StudentEnrollServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private static final String TOKEN_KEY = "studentEnrollToken";

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

            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null || !"OPEN".equalsIgnoreCase(clazz.getStatus())) {
                resp.sendRedirect(req.getContextPath() + "/student/classes");
                return;
            }

            List<ClassSchedule> schedules = scheduleDAO.listByClass(classId);
            if (schedules.isEmpty() || clazz.getStartDate() == null) {
                Flash.error(req, "Lá»›p chÆ°a cÃ³ lá»‹ch há»c. Vui lÃ²ng chá»n lá»›p khÃ¡c.");
                resp.sendRedirect(req.getContextPath() + "/student/classes");
                return;
            }
            BigDecimal balance = walletDAO.getBalance(user.getStudentId());

            req.setAttribute("clazz", clazz);
            req.setAttribute("schedules", schedules);
            req.setAttribute("balance", balance);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/student/enroll_confirm.jsp").forward(req, resp);
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

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/student/classes");
                return;
            }

            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null || !"OPEN".equalsIgnoreCase(clazz.getStatus())) {
                Flash.error(req, "Lớp không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/student/classes");
                return;
            }

            List<ClassSchedule> schedules2 = scheduleDAO.listByClass(classId);
            if (schedules2.isEmpty() || clazz.getStartDate() == null) {
                Flash.error(req, "Lá»›p chÆ°a cÃ³ lá»‹ch há»c. Vui lÃ²ng chá»n lá»›p khÃ¡c.");
                resp.sendRedirect(req.getContextPath() + "/student/classes");
                return;
            }

            if (enrollmentDAO.hasScheduleConflict(user.getStudentId(), classId)) {
                Flash.error(req, "Bạn bị trùng lịch với lớp khác.");
                resp.sendRedirect(req.getContextPath() + "/student/enroll?classId=" + classId);
                return;
            }

            BigDecimal fee = clazz.getStandardFee() == null ? BigDecimal.ZERO : clazz.getStandardFee();
            int enrollId;
            try {
                enrollId = enrollmentDAO.create(user.getStudentId(), classId, "PENDING");
            } catch (Exception ex) {
                Flash.error(req, "Không thể đăng ký (có thể bạn đã đăng ký lớp này).");
                resp.sendRedirect(req.getContextPath() + "/student/enroll?classId=" + classId);
                return;
            }

            int invoiceId = invoiceDAO.createInvoice(enrollId, fee, BigDecimal.ZERO, null);

            String pay = trim(req.getParameter("pay"));
            if ("WALLET".equalsIgnoreCase(pay)) {
                try {
                    walletDAO.debitForEnrollment(user.getStudentId(), enrollId, fee, user.getUserId(), "Pay invoice " + invoiceId);
                } catch (IllegalStateException ex) {
                    if ("INSUFFICIENT_BALANCE".equals(ex.getMessage())) {
                        Flash.error(req, "Số dư ví không đủ. Vui lòng nạp thêm tiền hoặc liên hệ tư vấn.");
                        resp.sendRedirect(req.getContextPath() + "/student/wallet");
                        return;
                    }
                    throw ex;
                }

                paymentDAO.addPayment(invoiceId, fee, "WALLET", "WALLET", user.getUserId());
                enrollmentDAO.setStatus(enrollId, "ACTIVE");
                Flash.success(req, "Đăng ký thành công và đã trừ tiền từ ví.");
                resp.sendRedirect(req.getContextPath() + "/student/timetable");
                return;
            }

            if ("PAYOS".equalsIgnoreCase(pay)) {
                Flash.success(req, "Vui lòng thanh toán bằng PayOS để hoàn tất đăng ký.");
                resp.sendRedirect(req.getContextPath() + "/student/pay/payos?invoiceId=" + invoiceId);
                return;
            }

            Flash.success(req, "Đã tạo đăng ký ở trạng thái chờ thanh toán. Vui lòng liên hệ tư vấn để xác nhận nộp tiền.");
            resp.sendRedirect(req.getContextPath() + "/student/fees");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
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
