package DAO;

import Model.Invoice;
import Util.CodeUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO extends DBContext {
    public List<Invoice> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT i.invoice_id, i.enroll_id, i.invoice_code, i.total_amount, i.discount_amount, i.paid_amount,
                       i.status, i.issued_at, i.issued_by,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_name, cr.course_name
                FROM dbo.invoices i
                JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE (? IS NULL OR i.status = ?)
                  AND (? IS NULL OR i.invoice_code LIKE ? OR s.full_name LIKE ? OR s.phone LIKE ?)
                ORDER BY i.invoice_id ASC
                """;
        List<Invoice> result = new ArrayList<>();
        String like = q == null ? null : "%" + q + "%";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Invoice findById(int invoiceId) throws Exception {
        String sql = """
                SELECT i.invoice_id, i.enroll_id, i.invoice_code, i.total_amount, i.discount_amount, i.paid_amount,
                       i.status, i.issued_at, i.issued_by,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_name, cr.course_name
                FROM dbo.invoices i
                JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE i.invoice_id = ?
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

    public Invoice findByEnrollId(int enrollId) throws Exception {
        String sql = """
                SELECT i.invoice_id, i.enroll_id, i.invoice_code, i.total_amount, i.discount_amount, i.paid_amount,
                       i.status, i.issued_at, i.issued_by,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_name, cr.course_name
                FROM dbo.invoices i
                JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE i.enroll_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int createInvoice(int enrollId, BigDecimal total, BigDecimal discount, Integer issuedByUserId) throws Exception {
        String sql = """
                INSERT INTO dbo.invoices(enroll_id, invoice_code, total_amount, discount_amount, paid_amount, status, issued_by)
                VALUES(?, ?, ?, ?, 0, N'UNPAID', ?)
                """;
        String code = CodeUtil.invoiceCode();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollId);
            ps.setString(2, code);
            ps.setBigDecimal(3, total);
            ps.setBigDecimal(4, discount);
            if (issuedByUserId == null) ps.setNull(5, java.sql.Types.INTEGER);
            else ps.setInt(5, issuedByUserId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void updatePaidAmountAndStatus(Connection con, int invoiceId, BigDecimal paidAmount, String status) throws Exception {
        String sql = "UPDATE dbo.invoices SET paid_amount = ?, status = ? WHERE invoice_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, paidAmount);
            ps.setString(2, status);
            ps.setInt(3, invoiceId);
            ps.executeUpdate();
        }
    }

    public InvoiceAmounts getAmounts(Connection con, int invoiceId) throws Exception {
        String sql = "SELECT total_amount, discount_amount, paid_amount, status FROM dbo.invoices WHERE invoice_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                InvoiceAmounts a = new InvoiceAmounts();
                a.total = rs.getBigDecimal("total_amount");
                a.discount = rs.getBigDecimal("discount_amount");
                a.paid = rs.getBigDecimal("paid_amount");
                a.status = rs.getString("status");
                return a;
            }
        }
    }

    public static final class InvoiceAmounts {
        public BigDecimal total;
        public BigDecimal discount;
        public BigDecimal paid;
        public String status;
    }

    private static Invoice map(ResultSet rs) throws Exception {
        Invoice i = new Invoice();
        i.setInvoiceId(rs.getInt("invoice_id"));
        i.setEnrollId(rs.getInt("enroll_id"));
        i.setInvoiceCode(rs.getString("invoice_code"));
        i.setTotalAmount(rs.getBigDecimal("total_amount"));
        i.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        i.setPaidAmount(rs.getBigDecimal("paid_amount"));
        i.setStatus(rs.getString("status"));
        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null) i.setIssuedAt(issuedAt.toInstant());
        int issuedBy = rs.getInt("issued_by");
        i.setIssuedBy(rs.wasNull() ? null : issuedBy);
        i.setStudentName(rs.getString("student_name"));
        i.setStudentPhone(rs.getString("student_phone"));
        i.setClassName(rs.getString("class_name"));
        i.setCourseName(rs.getString("course_name"));
        return i;
    }
}
