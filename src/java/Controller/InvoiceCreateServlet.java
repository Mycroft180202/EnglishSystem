package Controller;

import DAO.EnrollmentDAO;
import DAO.InvoiceDAO;
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

@WebServlet("/accounting/invoices/create")
public class InvoiceCreateServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private static final String TOKEN_KEY = "invoiceCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            List<Enrollment> enrollments = enrollmentDAO.listAll(null, null, "ACTIVE");
            req.setAttribute("enrollments", enrollments);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_form.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices");
                return;
            }

            int enrollId = parseInt(req.getParameter("enrollId"), 0);
            BigDecimal total = parseDecimal(req.getParameter("totalAmount"), null);
            BigDecimal discount = parseDecimal(req.getParameter("discountAmount"), BigDecimal.ZERO);

            if (enrollId <= 0 || total == null) {
                req.setAttribute("error", "Vui lòng chọn đăng ký và nhập tổng tiền.");
                req.setAttribute("enrollId", enrollId);
                req.setAttribute("totalAmount", req.getParameter("totalAmount"));
                req.setAttribute("discountAmount", req.getParameter("discountAmount"));
                req.setAttribute("enrollments", enrollmentDAO.listAll(null, null, "ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_form.jsp").forward(req, resp);
                return;
            }
            if (total.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(total) > 0) {
                req.setAttribute("error", "Số tiền không hợp lệ.");
                req.setAttribute("enrollId", enrollId);
                req.setAttribute("totalAmount", req.getParameter("totalAmount"));
                req.setAttribute("discountAmount", req.getParameter("discountAmount"));
                req.setAttribute("enrollments", enrollmentDAO.listAll(null, null, "ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_form.jsp").forward(req, resp);
                return;
            }

            if (invoiceDAO.findByEnrollId(enrollId) != null) {
                req.setAttribute("error", "Đăng ký này đã có hóa đơn.");
                req.setAttribute("enrollId", enrollId);
                req.setAttribute("totalAmount", req.getParameter("totalAmount"));
                req.setAttribute("discountAmount", req.getParameter("discountAmount"));
                req.setAttribute("enrollments", enrollmentDAO.listAll(null, null, "ACTIVE"));
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_form.jsp").forward(req, resp);
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer issuedBy = actor == null ? null : actor.getUserId();
            int invoiceId = invoiceDAO.createInvoice(enrollId, total, discount, issuedBy);
            Flash.success(req, "Tạo hóa đơn thành công.");
            resp.sendRedirect(req.getContextPath() + "/accounting/invoices/view?id=" + invoiceId);
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

    private static BigDecimal parseDecimal(String s, BigDecimal fallback) {
        if (s == null) return fallback;
        String t = s.trim().replace(",", "");
        if (t.isEmpty()) return fallback;
        try {
            return new BigDecimal(t);
        } catch (Exception ex) {
            return fallback;
        }
    }
}

