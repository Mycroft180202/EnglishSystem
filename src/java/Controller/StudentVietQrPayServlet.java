package Controller;

import DAO.EnrollmentDAO;
import DAO.InvoiceDAO;
import DAO.StudentFeeDAO;
import DAO.VietQrPaymentDAO;
import DAO.VietQrPaymentDAO.IntentRow;
import Model.Invoice;
import Model.User;
import Util.Flash;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;

// Disabled: migrated to PayOS (/student/pay/payos).
public class StudentVietQrPayServlet extends HttpServlet {
    private final StudentFeeDAO feeDAO = new StudentFeeDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final VietQrPaymentDAO vietQrDAO = new VietQrPaymentDAO();
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write("VietQR page has been disabled. Use PayOS instead.");
        return;

        /* unreachable legacy code kept for reference
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

            int invoiceId = parseInt(req.getParameter("invoiceId"), -1);
            Invoice invoice = invoiceId > 0 ? invoiceDAO.findById(invoiceId) : null;
            if (invoice == null) {
                resp.sendRedirect(req.getContextPath() + "/student/fees");
                return;
            }

            // Ensure invoice belongs to the current student
            boolean owns = feeDAO.listInvoicesByStudent(user.getStudentId()).stream().anyMatch(i -> i.getInvoiceId() == invoiceId);
            if (!owns) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            BigDecimal remaining = invoice.getRemainingAmount() == null ? BigDecimal.ZERO : invoice.getRemainingAmount();
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.success(req, "Hóa đơn đã được thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/student/fees");
                return;
            }

            IntentRow intent = vietQrDAO.findPendingByInvoice(invoiceId);
            if (intent == null) {
                String ref = "INV" + invoiceId + "_" + randomDigits(8);
                intent = vietQrDAO.create(invoiceId, invoice.getEnrollId(), remaining, ref);
            }

            String qrInfo = "ENGLISH-" + intent.getQrRef();
            String qrUrl = ""; // no longer used

            req.setAttribute("invoice", invoice);
            req.setAttribute("remaining", remaining);
            req.setAttribute("qrRef", intent.getQrRef());
            req.setAttribute("qrInfo", qrInfo);
            req.setAttribute("qrUrl", qrUrl);
            req.getRequestDispatcher("/WEB-INF/views/student/vietqr_pay.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        */
    }

    private static String randomDigits(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }
}
