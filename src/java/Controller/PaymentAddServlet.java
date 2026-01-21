package Controller;

import DAO.PaymentDAO;
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

@WebServlet("/accounting/payments/add")
public class PaymentAddServlet extends HttpServlet {
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private static final String TOKEN_KEY = "paymentAddToken";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int invoiceId = parseInt(req.getParameter("invoiceId"), -1);
            if (invoiceId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices/view?id=" + invoiceId);
                return;
            }

            BigDecimal amount = parseDecimal(req.getParameter("amount"));
            String method = trim(req.getParameter("method")).toUpperCase();
            String txnRef = trim(req.getParameter("txnRef"));

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.error(req, "Số tiền không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices/view?id=" + invoiceId);
                return;
            }
            if (!(method.equals("CASH") || method.equals("TRANSFER") || method.equals("CARD"))) {
                Flash.error(req, "Phương thức không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices/view?id=" + invoiceId);
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer receivedBy = actor == null ? null : actor.getUserId();
            paymentDAO.addPayment(invoiceId, amount, method, txnRef, receivedBy);
            Flash.success(req, "Đã ghi nhận thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/accounting/invoices/view?id=" + invoiceId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    public static String issueToken(HttpServletRequest req) {
        return FormToken.issue(req, TOKEN_KEY);
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

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

