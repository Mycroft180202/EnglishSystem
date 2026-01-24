package Controller;

import DAO.EnrollmentDAO;
import DAO.PayOsPaymentDAO;
import DAO.PayOsPaymentDAO.IntentRow;
import DAO.PayOsWalletTopupDAO;
import DAO.PaymentDAO;
import DAO.PaymentRequestDAO;
import DAO.WalletDAO;
import Util.HmacUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/payos/webhook")
public class PayOsWebhookServlet extends HttpServlet {
    private final PayOsPaymentDAO payOsDAO = new PayOsPaymentDAO();
    private final PayOsWalletTopupDAO walletTopupDAO = new PayOsWalletTopupDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentRequestDAO paymentRequestDAO = new PaymentRequestDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final WalletDAO walletDAO = new WalletDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            byte[] rawBody = req.getInputStream().readAllBytes();
            verifySignatureOrThrow(req, rawBody);

            String bodyText = new String(rawBody, StandardCharsets.UTF_8);

            long orderCode = parseLong(extractJson(bodyText, "orderCode"), -1);
            if (orderCode <= 0) orderCode = parseLong(extractJson(bodyText, "order_code"), -1);
            if (orderCode <= 0) {
                resp.setStatus(400);
                resp.getWriter().write("missing orderCode");
                return;
            }

            String status = firstNonEmpty(
                    extractJson(bodyText, "status"),
                    extractJson(bodyText, "code")
            ).toUpperCase();

            // Accept common success markers; adjust as needed.
            if (!(status.contains("PAID") || status.contains("SUCCESS") || "00".equals(status))) {
                resp.setStatus(200);
                resp.getWriter().write("{\"ok\":true,\"ignored\":true}");
                return;
            }

            IntentRow intent = payOsDAO.findByOrderCode(orderCode);
            BigDecimal amount = parseDecimalOrNull(firstNonEmpty(
                    extractJson(bodyText, "amount"),
                    extractJson(bodyText, "amountVnd"),
                    extractJson(bodyText, "transactionAmount")
            ));

            String txnRef = firstNonEmpty(
                    extractJson(bodyText, "paymentLinkId"),
                    extractJson(bodyText, "transactionId"),
                    extractJson(bodyText, "reference"),
                    extractJson(bodyText, "txnRef")
            );
            if (txnRef != null && txnRef.isBlank()) txnRef = null;

            if (intent != null) {
                if (amount != null && intent.getAmount() != null && amount.compareTo(intent.getAmount()) != 0) {
                    resp.setStatus(400);
                    resp.getWriter().write("amount mismatch");
                    return;
                }

                boolean updated = payOsDAO.markPaid(orderCode, txnRef, bodyText);
                if (updated) {
                    paymentRequestDAO.rejectPendingByInvoice(intent.getInvoiceId(), null, "Auto rejected: paid via PayOS");
                    try {
                        paymentDAO.addPayment(intent.getInvoiceId(), intent.getAmount(), "PAYOS", txnRef, null);
                    } catch (IllegalStateException ex) {
                        // Ignore: invoice may have been settled already by another method.
                    }
                    // Keep enrollment as PENDING; Admin/Consultant will approve the student into the class.
                }
            } else {
                PayOsWalletTopupDAO.IntentRow topup = walletTopupDAO.findByOrderCode(orderCode);
                if (topup == null) {
                    resp.setStatus(404);
                    resp.getWriter().write("not found");
                    return;
                }
                if (amount != null && topup.getAmount() != null && amount.compareTo(topup.getAmount()) != 0) {
                    resp.setStatus(400);
                    resp.getWriter().write("amount mismatch");
                    return;
                }

                boolean updated = walletTopupDAO.markPaid(orderCode, txnRef, bodyText);
                if (updated) {
                    walletDAO.topup(topup.getStudentId(), topup.getAmount(), null, "PayOS topup " + orderCode);
                }
            }

            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"ok\":true}");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static void verifySignatureOrThrow(HttpServletRequest req, byte[] rawBody) throws IOException {
        boolean skip = "true".equalsIgnoreCase(System.getProperty("PAYOS_WEBHOOK_SKIP_VERIFY", "false"));
        if (skip) return;

        String checksumKey = firstNonEmpty(System.getProperty("PAYOS_CHECKSUM_KEY", ""), System.getenv("PAYOS_CHECKSUM_KEY"));
        if (checksumKey == null || checksumKey.isBlank()) {
            throw new IOException("Missing PAYOS_CHECKSUM_KEY system property");
        }

        String sig = firstNonEmpty(
                req.getHeader("x-payos-signature"),
                req.getHeader("X-PayOS-Signature"),
                req.getHeader("X-Signature"),
                req.getHeader("X-Webhook-Signature")
        );
        if (sig == null || sig.isBlank()) {
            // Some PayOS configs send signature in body; allow skipping header verification.
            return;
        }

        sig = sig.trim();
        if (sig.toLowerCase().startsWith("sha256=")) sig = sig.substring("sha256=".length()).trim();
        sig = sig.toLowerCase();

        String expected = HmacUtil.hmacSha256Hex(checksumKey, rawBody);
        if (!HmacUtil.constantTimeEqualsHex(expected, sig)) throw new IOException("Invalid signature");
    }

    private static String extractJson(String body, String key) {
        if (body == null || body.isEmpty() || key == null || key.isEmpty()) return "";
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(body);
        if (m.find()) return m.group(1);
        Pattern p2 = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(body);
        if (m2.find()) return m2.group(1);
        return "";
    }

    private static BigDecimal parseDecimalOrNull(String s) {
        if (s == null) return null;
        String t = s.trim().replace(",", "");
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return "";
    }
}
