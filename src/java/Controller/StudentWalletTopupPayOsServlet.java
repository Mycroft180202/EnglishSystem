package Controller;

import DAO.PayOsWalletTopupDAO;
import DAO.PayOsWalletTopupDAO.IntentRow;
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

@WebServlet("/student/wallet/topup/payos")
public class StudentWalletTopupPayOsServlet extends HttpServlet {
    private final PayOsWalletTopupDAO topupDAO = new PayOsWalletTopupDAO();
    private static final SecureRandom RNG = new SecureRandom();

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

            BigDecimal amount = parseDecimal(req.getParameter("amount"));
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.error(req, "Số tiền không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/student/wallet");
                return;
            }

            // Round to VND integer for PayOS request
            long amountVnd = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
            if (amountVnd <= 0) {
                Flash.error(req, "Số tiền không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/student/wallet");
                return;
            }

            long orderCode = buildTopupOrderCode(user.getStudentId());
            IntentRow intent = topupDAO.create(user.getStudentId(), new BigDecimal(amountVnd), orderCode);

            String desc = "ENGLISH-TOPUP-" + user.getStudentId();
            String baseUrl = req.getRequestURL().toString().replace(req.getRequestURI(), req.getContextPath());
            String returnUrl = baseUrl + "/student/wallet";
            String cancelUrl = baseUrl + "/student/wallet";

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
            Flash.error(req, "Không thể tạo link nạp ví PayOS. " + (msg.isEmpty() ? "" : ("Lỗi: " + msg)));
            resp.sendRedirect(req.getContextPath() + "/student/wallet");
        }
    }

    private static long buildTopupOrderCode(int studentId) {
        // Namespace wallet topups away from invoice payments.
        long suffix = RNG.nextInt(900000) + 100000; // 6 digits
        return 8_000_000_000_000L + (long) studentId * 1_000_000L + suffix;
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
}

