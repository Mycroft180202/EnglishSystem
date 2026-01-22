package Controller;

import DAO.EnrollmentDAO;
import DAO.EnrollmentDAO.EnrollPayInfo;
import DAO.InvoiceDAO;
import DAO.PaymentRequestDAO;
import Model.Invoice;
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

@WebServlet("/consultant/payment-requests/create")
public class ConsultantPaymentRequestCreateServlet extends HttpServlet {
    private final PaymentRequestDAO requestDAO = new PaymentRequestDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private static final String TOKEN_KEY = "consultantPayReqToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            int invoiceId = parseInt(req.getParameter("invoiceId"), -1);
            Invoice invoice = invoiceId > 0 ? invoiceDAO.findById(invoiceId) : null;
            if (invoice == null) {
                resp.sendRedirect(req.getContextPath() + "/consultant/enrollments");
                return;
            }

            EnrollPayInfo info = enrollmentDAO.findPayInfo(invoice.getEnrollId());
            BigDecimal remaining = invoice.getRemainingAmount() == null ? BigDecimal.ZERO : invoice.getRemainingAmount();

            req.setAttribute("invoice", invoice);
            req.setAttribute("payInfo", info);
            req.setAttribute("remaining", remaining);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/consultant/payment_request_create.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/consultant/enrollments");
                return;
            }

            int invoiceId = parseInt(req.getParameter("invoiceId"), -1);
            Invoice invoice = invoiceId > 0 ? invoiceDAO.findById(invoiceId) : null;
            if (invoice == null) {
                resp.sendRedirect(req.getContextPath() + "/consultant/enrollments");
                return;
            }

            BigDecimal amount = parseDecimal(req.getParameter("amount"));
            String method = trim(req.getParameter("method")).toUpperCase();
            String note = trim(req.getParameter("note"));

            BigDecimal remaining = invoice.getRemainingAmount() == null ? BigDecimal.ZERO : invoice.getRemainingAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(remaining) > 0) {
                Flash.error(req, "Số tiền không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/consultant/payment-requests/create?invoiceId=" + invoiceId);
                return;
            }
            if (!method.equals("CASH")) {
                Flash.error(req, "Phương thức không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/consultant/payment-requests/create?invoiceId=" + invoiceId);
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer createdBy = actor == null ? null : actor.getUserId();
            requestDAO.create(invoice.getInvoiceId(), invoice.getEnrollId(), amount, method, note, createdBy);
            Flash.success(req, "Đã gửi yêu cầu thu tiền cho kế toán.");
            resp.sendRedirect(req.getContextPath() + "/consultant/enrollments");
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

    private static BigDecimal parseDecimal(String s) {
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
