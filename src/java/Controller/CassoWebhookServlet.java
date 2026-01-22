package Controller;

import DAO.EnrollmentDAO;
import DAO.PaymentDAO;
import DAO.VietQrPaymentDAO;
import DAO.VietQrPaymentDAO.IntentRow;
import Util.HmacUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CassoWebhookServlet extends HttpServlet {
    private static final Pattern MARKER = Pattern.compile("ENGLISH-([A-Za-z0-9_\\-]+)");

    private final VietQrPaymentDAO vietQrDAO = new VietQrPaymentDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write("Casso webhook has been disabled. Use /api/payos/webhook instead.");
        return;

        /* unreachable legacy code kept for reference
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            byte[] rawBody = req.getInputStream().readAllBytes();
            verifyAuthOrThrow(req, rawBody);

            String bodyText = new String(rawBody, StandardCharsets.UTF_8);

            Set<String> qrRefs = new HashSet<>();
            Matcher matcher = MARKER.matcher(bodyText);
            while (matcher.find()) {
                String qrRef = trim(matcher.group(1));
                if (!qrRef.isEmpty()) qrRefs.add(qrRef);
            }

            int updatedCount = 0;
            int processedCount = 0;

            for (String qrRef : qrRefs) {
                processedCount++;
                IntentRow intent = vietQrDAO.findByQrRef(qrRef);
                if (intent == null) continue;

                String obj = extractEnclosingJsonObject(bodyText, qrRef);
                BigDecimal amount = parseDecimalOrNull(firstNonEmpty(
                        extractJson(obj, "amount"),
                        extractJson(obj, "amountVnd"),
                        extractJson(obj, "transactionAmount")
                ));
                if (amount != null) amount = amount.abs();

                if (amount != null && intent.getAmount() != null && amount.compareTo(intent.getAmount()) != 0) {
                    continue; // ignore mismatched transfers
                }

                String txnRef = firstNonEmpty(
                        extractJson(obj, "tid"),
                        extractJson(obj, "id"),
                        extractJson(obj, "refNo"),
                        extractJson(obj, "reference"),
                        extractJson(obj, "transactionId"),
                        extractJson(obj, "txnRef")
                );
                if (txnRef != null && txnRef.isBlank()) txnRef = null;

                boolean updated = vietQrDAO.markPaid(qrRef, txnRef, bodyText);
                if (updated) {
                    paymentDAO.addPayment(intent.getInvoiceId(), intent.getAmount(), "VIETQR", txnRef, null);
                    enrollmentDAO.setStatus(intent.getEnrollId(), "ACTIVE");
                    updatedCount++;
                }
            }

            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"ok\":true,\"processed\":" + processedCount + ",\"updated\":" + updatedCount + "}");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        */
    }

    private static void verifyAuthOrThrow(HttpServletRequest req, byte[] rawBody) throws IOException {
        boolean skip = "true".equalsIgnoreCase(System.getProperty("CASSO_WEBHOOK_SKIP_VERIFY", "false"));
        if (skip) return;

        String expectedToken = System.getProperty("CASSO_SECURE_TOKEN", "");
        if (expectedToken == null || expectedToken.isBlank()) {
            throw new IOException("Missing CASSO_SECURE_TOKEN system property");
        }

        String token = firstNonEmpty(
                req.getHeader("secure-token"),
                req.getHeader("Secure-Token"),
                req.getHeader("X-Casso-Token"),
                req.getParameter("secure_token"),
                req.getParameter("token")
        );
        if (token == null || token.isBlank() || !expectedToken.trim().equals(token.trim())) {
            throw new IOException("Invalid Casso secure token");
        }

        String secret = System.getProperty("CASSO_WEBHOOK_SECRET", "");
        String sig = firstNonEmpty(req.getHeader("X-Casso-Signature"), req.getHeader("X-Signature"));
        if (sig == null || sig.isBlank() || secret == null || secret.isBlank()) return;

        sig = sig.trim();
        if (sig.toLowerCase().startsWith("sha256=")) sig = sig.substring("sha256=".length()).trim();
        sig = sig.toLowerCase();
        String expected = HmacUtil.hmacSha256Hex(secret, rawBody);
        if (!HmacUtil.constantTimeEqualsHex(expected, sig)) throw new IOException("Invalid signature");
    }

    private static String extractEnclosingJsonObject(String body, String qrRef) {
        if (body == null || body.isEmpty() || qrRef == null || qrRef.isEmpty()) return "";
        int idx = body.indexOf("ENGLISH-" + qrRef);
        if (idx < 0) return "";

        int start = body.lastIndexOf('{', idx);
        if (start < 0) return "";
        int end = findObjectEnd(body, start);
        if (end < 0) return "";
        return body.substring(start, end + 1);
    }

    private static int findObjectEnd(String s, int start) {
        boolean inStr = false;
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && !isEscaped(s, i)) inStr = !inStr;
            if (inStr) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static boolean isEscaped(String s, int quoteIdx) {
        int bs = 0;
        for (int i = quoteIdx - 1; i >= 0 && s.charAt(i) == '\\'; i--) bs++;
        return (bs % 2) == 1;
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

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
