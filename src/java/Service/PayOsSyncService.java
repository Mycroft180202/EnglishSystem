package Service;

import DAO.EnrollmentDAO;
import DAO.PayOsPaymentDAO;
import DAO.PayOsWalletTopupDAO;
import DAO.PaymentDAO;
import DAO.PaymentRequestDAO;
import DAO.WalletDAO;
import Util.PayOsUtil;
import java.math.BigDecimal;

public class PayOsSyncService {
    private final PayOsPaymentDAO payOsDAO = new PayOsPaymentDAO();
    private final PayOsWalletTopupDAO walletTopupDAO = new PayOsWalletTopupDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentRequestDAO paymentRequestDAO = new PaymentRequestDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final WalletDAO walletDAO = new WalletDAO();

    public static final class SyncResult {
        public final boolean updated;
        public final boolean paid;
        public final String message;

        public SyncResult(boolean updated, boolean paid, String message) {
            this.updated = updated;
            this.paid = paid;
            this.message = message;
        }
    }

    public SyncResult syncByReturnParams(String orderCodeStr, String paymentLinkId, String status, String code, String cancel) {
        long orderCode = parseLong(orderCodeStr, -1);
        if (orderCode <= 0) return new SyncResult(false, false, "missing orderCode");

        boolean hintedPaid = "PAID".equalsIgnoreCase(trim(status)) || "00".equals(trim(code));
        boolean hintedCancel = "true".equalsIgnoreCase(trim(cancel));

        String raw;
        try {
            raw = PayOsUtil.getPaymentRequest(PayOsUtil.loadFromSystemProperties(), Long.toString(orderCode));
        } catch (Exception ex) {
            // Fallback to paymentLinkId (if PayOS supports GET by id)
            if (paymentLinkId != null && !trim(paymentLinkId).isEmpty()) {
                try {
                    raw = PayOsUtil.getPaymentRequest(PayOsUtil.loadFromSystemProperties(), trim(paymentLinkId));
                } catch (Exception ex2) {
                    return new SyncResult(false, hintedPaid && !hintedCancel, "cannot query PayOS: " + safeMsg(ex2));
                }
            } else {
                return new SyncResult(false, hintedPaid && !hintedCancel, "cannot query PayOS: " + safeMsg(ex));
            }
        }

        boolean paid = PayOsUtil.isPaidResponse(raw);
        if (!paid) {
            return new SyncResult(false, hintedPaid && !hintedCancel, "payment not PAID yet");
        }

        try {
            boolean updated = applyPaid(orderCode, paymentLinkId, raw);
            return new SyncResult(updated, true, updated ? "updated" : "already updated");
        } catch (Exception ex) {
            return new SyncResult(false, true, "update failed: " + safeMsg(ex));
        }
    }

    private boolean applyPaid(long orderCode, String paymentLinkId, String rawPayload) throws Exception {
        String txnRef = trim(paymentLinkId);
        if (txnRef.isEmpty()) txnRef = Long.toString(orderCode);

        PayOsPaymentDAO.IntentRow intent = payOsDAO.findByOrderCode(orderCode);
        if (intent != null) {
            boolean updated = payOsDAO.markPaid(orderCode, txnRef, rawPayload);
            if (updated) {
                paymentRequestDAO.rejectPendingByInvoice(intent.getInvoiceId(), null, "Auto rejected: paid via PayOS");
                try {
                    paymentDAO.addPayment(intent.getInvoiceId(), intent.getAmount(), "PAYOS", txnRef, null);
                } catch (IllegalStateException ex) {
                    // Ignore: invoice may have been settled already by another method.
                }
                enrollmentDAO.setStatus(intent.getEnrollId(), "ACTIVE");
            }
            return updated;
        }

        PayOsWalletTopupDAO.IntentRow topup = walletTopupDAO.findByOrderCode(orderCode);
        if (topup != null) {
            boolean updated = walletTopupDAO.markPaid(orderCode, txnRef, rawPayload);
            if (updated) {
                BigDecimal amt = topup.getAmount();
                walletDAO.topup(topup.getStudentId(), amt, null, "PayOS topup " + orderCode);
            }
            return updated;
        }

        throw new IllegalStateException("orderCode not found in local intents");
    }

    private static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeMsg(Exception ex) {
        String m = ex.getMessage() == null ? ex.toString() : ex.getMessage();
        m = m.trim();
        if (m.length() > 180) m = m.substring(0, 180) + "...";
        return m;
    }
}
