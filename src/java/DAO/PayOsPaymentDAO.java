package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;

public class PayOsPaymentDAO extends DBContext {
    public static final class IntentRow {
        private long intentId;
        private int invoiceId;
        private int enrollId;
        private BigDecimal amount;
        private long orderCode;
        private String status;
        private Instant createdAt;
        private Instant paidAt;
        private String txnRef;

        public long getIntentId() { return intentId; }
        public int getInvoiceId() { return invoiceId; }
        public int getEnrollId() { return enrollId; }
        public BigDecimal getAmount() { return amount; }
        public long getOrderCode() { return orderCode; }
        public String getStatus() { return status; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getPaidAt() { return paidAt; }
        public String getTxnRef() { return txnRef; }
    }

    public IntentRow findPendingByInvoice(int invoiceId) throws Exception {
        String sql = """
                SELECT TOP 1 intent_id, invoice_id, enroll_id, amount, order_code, status, created_at, paid_at, txn_ref
                FROM dbo.payos_payment_intents
                WHERE invoice_id = ? AND status = N'PENDING'
                ORDER BY intent_id DESC
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public IntentRow create(int invoiceId, int enrollId, BigDecimal amount, long orderCode) throws Exception {
        String sql = """
                INSERT INTO dbo.payos_payment_intents(invoice_id, enroll_id, amount, order_code, status)
                VALUES(?, ?, ?, ?, N'PENDING')
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoiceId);
            ps.setInt(2, enrollId);
            ps.setBigDecimal(3, amount);
            ps.setLong(4, orderCode);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
            }
        }
        return findByOrderCode(orderCode);
    }

    public IntentRow findByOrderCode(long orderCode) throws Exception {
        String sql = """
                SELECT intent_id, invoice_id, enroll_id, amount, order_code, status, created_at, paid_at, txn_ref
                FROM dbo.payos_payment_intents
                WHERE order_code = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public boolean markPaid(long orderCode, String txnRef, String rawPayload) throws Exception {
        String sql = """
                UPDATE dbo.payos_payment_intents
                SET status = N'PAID', paid_at = SYSUTCDATETIME(), txn_ref = ?, raw_payload = ?
                WHERE order_code = ? AND status = N'PENDING'
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (txnRef == null || txnRef.isBlank()) ps.setNull(1, Types.NVARCHAR);
            else ps.setString(1, txnRef.trim());
            if (rawPayload == null || rawPayload.isBlank()) ps.setNull(2, Types.NVARCHAR);
            else ps.setString(2, rawPayload);
            ps.setLong(3, orderCode);
            return ps.executeUpdate() == 1;
        }
    }

    private static IntentRow map(ResultSet rs) throws Exception {
        IntentRow r = new IntentRow();
        r.intentId = rs.getLong("intent_id");
        r.invoiceId = rs.getInt("invoice_id");
        r.enrollId = rs.getInt("enroll_id");
        r.amount = rs.getBigDecimal("amount");
        r.orderCode = rs.getLong("order_code");
        r.status = rs.getString("status");
        java.sql.Timestamp c = rs.getTimestamp("created_at");
        if (c != null) r.createdAt = c.toInstant();
        java.sql.Timestamp p = rs.getTimestamp("paid_at");
        if (p != null) r.paidAt = p.toInstant();
        r.txnRef = rs.getString("txn_ref");
        return r;
    }
}

