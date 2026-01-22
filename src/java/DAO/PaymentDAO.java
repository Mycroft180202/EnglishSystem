package DAO;

import Model.Payment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO extends DBContext {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    public List<Payment> listByInvoice(int invoiceId) throws Exception {
        String sql = """
                SELECT payment_id, invoice_id, amount, method, txn_ref, paid_at, received_by
                FROM dbo.payments
                WHERE invoice_id = ?
                ORDER BY payment_id ASC
                """;
        List<Payment> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public BigDecimal addPayment(int invoiceId, BigDecimal amount, String method, String txnRef, Integer receivedByUserId) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount invalid");

        String insertSql = """
                INSERT INTO dbo.payments(invoice_id, amount, method, txn_ref, received_by)
                VALUES(?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection()) {
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                InvoiceDAO.InvoiceAmounts amounts = getAmountsForUpdate(con, invoiceId);
                if (amounts == null) throw new IllegalArgumentException("Invoice not found");
                if ("VOID".equalsIgnoreCase(amounts.status)) throw new IllegalStateException("Invoice is VOID");

                BigDecimal paid = amounts.paid == null ? BigDecimal.ZERO : amounts.paid;
                BigDecimal newPaid = paid.add(amount);
                BigDecimal total = amounts.total == null ? BigDecimal.ZERO : amounts.total;
                BigDecimal discount = amounts.discount == null ? BigDecimal.ZERO : amounts.discount;
                BigDecimal due = total.subtract(discount);
                BigDecimal remaining = due.subtract(paid);

                if (remaining.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalStateException("Invoice already PAID");

                String cleanedTxn = (txnRef == null || txnRef.isBlank()) ? null : txnRef.trim();
                if (cleanedTxn != null && paymentExists(con, invoiceId, method, cleanedTxn)) {
                    con.rollback();
                    con.setAutoCommit(oldAutoCommit);
                    return BigDecimal.ZERO;
                }

                BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
                BigDecimal applied = normalized.min(remaining).setScale(2, RoundingMode.HALF_UP);

                try (PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, invoiceId);
                    ps.setBigDecimal(2, applied);
                    ps.setString(3, method);
                    if (cleanedTxn == null) ps.setNull(4, Types.NVARCHAR);
                    else ps.setString(4, cleanedTxn);
                    if (receivedByUserId == null) ps.setNull(5, Types.INTEGER);
                    else ps.setInt(5, receivedByUserId);
                    ps.executeUpdate();
                }

                newPaid = paid.add(applied);
                if (newPaid.compareTo(due) > 0) newPaid = due;

                String newStatus;
                if (newPaid.compareTo(BigDecimal.ZERO) <= 0) newStatus = "UNPAID";
                else if (newPaid.compareTo(due) >= 0) newStatus = "PAID";
                else newStatus = "PARTIAL";

                invoiceDAO.updatePaidAmountAndStatus(con, invoiceId, newPaid, newStatus);
                con.commit();
                return applied;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    private static InvoiceDAO.InvoiceAmounts getAmountsForUpdate(Connection con, int invoiceId) throws Exception {
        String sql = """
                SELECT total_amount, discount_amount, paid_amount, status
                FROM dbo.invoices WITH (UPDLOCK, ROWLOCK)
                WHERE invoice_id = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                InvoiceDAO.InvoiceAmounts a = new InvoiceDAO.InvoiceAmounts();
                a.total = rs.getBigDecimal("total_amount");
                a.discount = rs.getBigDecimal("discount_amount");
                a.paid = rs.getBigDecimal("paid_amount");
                a.status = rs.getString("status");
                return a;
            }
        }
    }

    private static boolean paymentExists(Connection con, int invoiceId, String method, String txnRef) throws Exception {
        String sql = """
                SELECT TOP 1 1
                FROM dbo.payments
                WHERE invoice_id = ? AND method = ? AND txn_ref = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.setString(2, method);
            ps.setString(3, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Payment map(ResultSet rs) throws Exception {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(rs.getString("method"));
        p.setTxnRef(rs.getString("txn_ref"));
        Timestamp t = rs.getTimestamp("paid_at");
        if (t != null) p.setPaidAt(t.toInstant());
        int recv = rs.getInt("received_by");
        p.setReceivedBy(rs.wasNull() ? null : recv);
        return p;
    }
}
