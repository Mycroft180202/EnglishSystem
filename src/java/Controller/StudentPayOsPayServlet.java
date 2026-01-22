package Controller;

import DAO.InvoiceDAO;
import DAO.PayOsPaymentDAO;
import DAO.PayOsPaymentDAO.IntentRow;
import DAO.StudentFeeDAO;
import Model.Invoice;
import Model.User;
import Util.Flash;
import Util.PayOsUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;

@WebServlet("/student/pay/payos")
public class StudentPayOsPayServlet extends HttpServlet {
    private final StudentFeeDAO feeDAO = new StudentFeeDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PayOsPaymentDAO payOsDAO = new PayOsPaymentDAO();
    private static final SecureRandom RNG = new SecureRandom();

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

            int invoiceId = parseInt(req.getParameter("invoiceId"), -1);
            Invoice invoice = invoiceId > 0 ? invoiceDAO.findById(invoiceId) : null;
            if (invoice == null) {
                resp.sendRedirect(req.getContextPath() + "/student/fees");
                return;
            }

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

            IntentRow intent = payOsDAO.findPendingByInvoice(invoiceId);
            if (intent == null) {
                long orderCode = buildOrderCode(invoiceId);
                intent = payOsDAO.create(invoiceId, invoice.getEnrollId(), remaining, orderCode);
            }

            long amountVnd = remaining.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
            String desc = "ENGLISH-INV" + invoiceId;
            String baseUrl = req.getRequestURL().toString().replace(req.getRequestURI(), req.getContextPath());
            String returnUrl = baseUrl + "/student/fees";
            String cancelUrl = baseUrl + "/student/fees";

            String checkoutUrl = PayOsUtil.createPaymentLink(
                    PayOsUtil.loadFromSystemProperties(),
                    intent.getOrderCode(),
                    amountVnd,
                    desc,
                    returnUrl,
                    cancelUrl
            );

            resp.sendRedirect(checkoutUrl);
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().trim();
            if (msg.length() > 220) msg = msg.substring(0, 220) + "...";
            Flash.error(req, "Không thể tạo link thanh toán PayOS. " + (msg.isEmpty() ? "" : ("Lỗi: " + msg)));
            resp.sendRedirect(req.getContextPath() + "/student/fees");
        }
    }

    private static long buildOrderCode(int invoiceId) {
        // PayOS orderCode is numeric; keep it short enough but unique.
        long suffix = RNG.nextInt(900000) + 100000; // 6 digits
        return (long) invoiceId * 1_000_000L + suffix;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }
}
