package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PaymentRequestDAO extends DBContext {
    public static final class PaymentRequestRow {
        private int requestId;
        private int invoiceId;
        private int enrollId;
        private BigDecimal amount;
        private String method;
        private String status;
        private String note;
        private Instant createdAt;
        private Integer createdBy;
        private Instant decidedAt;
        private Integer decidedBy;
        private String studentName;
        private String className;
        private String invoiceCode;

        public int getRequestId() { return requestId; }
        public int getInvoiceId() { return invoiceId; }
        public int getEnrollId() { return enrollId; }
        public BigDecimal getAmount() { return amount; }
        public String getMethod() { return method; }
        public String getStatus() { return status; }
        public String getNote() { return note; }
        public Instant getCreatedAt() { return createdAt; }
        public Integer getCreatedBy() { return createdBy; }
        public Instant getDecidedAt() { return decidedAt; }
        public Integer getDecidedBy() { return decidedBy; }
        public String getStudentName() { return studentName; }
        public String getClassName() { return className; }
        public String getInvoiceCode() { return invoiceCode; }
    }

    public int create(int invoiceId, int enrollId, BigDecimal amount, String method, String note, Integer createdByUserId) throws Exception {
        String sql = """
                INSERT INTO dbo.payment_requests(invoice_id, enroll_id, amount, method, note, created_by)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoiceId);
            ps.setInt(2, enrollId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, method);
            ps.setString(5, note == null || note.isBlank() ? null : note.trim());
            if (createdByUserId == null) ps.setNull(6, java.sql.Types.INTEGER);
            else ps.setInt(6, createdByUserId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public List<PaymentRequestRow> listPending() throws Exception {
        String sql = """
                SELECT pr.request_id, pr.invoice_id, pr.enroll_id, pr.amount, pr.method, pr.status, pr.note,
                       pr.created_at, pr.created_by, pr.decided_at, pr.decided_by,
                       i.invoice_code,
                       s.full_name AS student_name,
                       c.class_name
                FROM dbo.payment_requests pr
                JOIN dbo.invoices i ON pr.invoice_id = i.invoice_id
                JOIN dbo.enrollments e ON pr.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                WHERE pr.status = N'PENDING'
                ORDER BY pr.created_at ASC, pr.request_id ASC
                """;
        List<PaymentRequestRow> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public PaymentRequestRow findById(int requestId) throws Exception {
        String sql = """
                SELECT pr.request_id, pr.invoice_id, pr.enroll_id, pr.amount, pr.method, pr.status, pr.note,
                       pr.created_at, pr.created_by, pr.decided_at, pr.decided_by,
                       i.invoice_code,
                       s.full_name AS student_name,
                       c.class_name
                FROM dbo.payment_requests pr
                JOIN dbo.invoices i ON pr.invoice_id = i.invoice_id
                JOIN dbo.enrollments e ON pr.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                WHERE pr.request_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public boolean markApproved(int requestId, Integer decidedByUserId) throws Exception {
        String sql = """
                UPDATE dbo.payment_requests
                SET status = N'APPROVED', decided_at = SYSUTCDATETIME(), decided_by = ?
                WHERE request_id = ? AND status = N'PENDING'
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (decidedByUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, decidedByUserId);
            ps.setInt(2, requestId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean markRejected(int requestId, Integer decidedByUserId) throws Exception {
        String sql = """
                UPDATE dbo.payment_requests
                SET status = N'REJECTED', decided_at = SYSUTCDATETIME(), decided_by = ?
                WHERE request_id = ? AND status = N'PENDING'
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (decidedByUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, decidedByUserId);
            ps.setInt(2, requestId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean markRejectedForce(int requestId, Integer decidedByUserId, String reason) throws Exception {
        String reasonTrim = reason == null ? "" : reason.trim();
        if (reasonTrim.length() > 200) reasonTrim = reasonTrim.substring(0, 200);
        String sql = """
                UPDATE dbo.payment_requests
                SET status = N'REJECTED',
                    decided_at = SYSUTCDATETIME(),
                    decided_by = ?,
                    note = CASE
                              WHEN ? = N'' THEN note
                              WHEN note IS NULL OR LTRIM(RTRIM(note)) = N'' THEN LEFT(?, 255)
                              ELSE LEFT(note + N' | ' + ?, 255)
                           END
                WHERE request_id = ? AND status IN (N'PENDING', N'APPROVED')
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (decidedByUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, decidedByUserId);
            ps.setString(2, reasonTrim);
            ps.setString(3, reasonTrim);
            ps.setString(4, reasonTrim);
            ps.setInt(5, requestId);
            return ps.executeUpdate() == 1;
        }
    }

    public int rejectPendingByInvoice(int invoiceId, Integer decidedByUserId, String reason) throws Exception {
        String reasonTrim = reason == null ? "" : reason.trim();
        if (reasonTrim.length() > 200) reasonTrim = reasonTrim.substring(0, 200);
        String sql = """
                UPDATE dbo.payment_requests
                SET status = N'REJECTED',
                    decided_at = SYSUTCDATETIME(),
                    decided_by = ?,
                    note = CASE
                              WHEN ? = N'' THEN note
                              WHEN note IS NULL OR LTRIM(RTRIM(note)) = N'' THEN LEFT(?, 255)
                              ELSE LEFT(note + N' | ' + ?, 255)
                           END
                WHERE invoice_id = ? AND status = N'PENDING'
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (decidedByUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, decidedByUserId);
            ps.setString(2, reasonTrim);
            ps.setString(3, reasonTrim);
            ps.setString(4, reasonTrim);
            ps.setInt(5, invoiceId);
            return ps.executeUpdate();
        }
    }

    private static PaymentRequestRow map(ResultSet rs) throws Exception {
        PaymentRequestRow r = new PaymentRequestRow();
        r.requestId = rs.getInt("request_id");
        r.invoiceId = rs.getInt("invoice_id");
        r.enrollId = rs.getInt("enroll_id");
        r.amount = rs.getBigDecimal("amount");
        r.method = rs.getString("method");
        r.status = rs.getString("status");
        r.note = rs.getString("note");
        Timestamp t1 = rs.getTimestamp("created_at");
        if (t1 != null) r.createdAt = t1.toInstant();
        int cb = rs.getInt("created_by");
        r.createdBy = rs.wasNull() ? null : cb;
        Timestamp t2 = rs.getTimestamp("decided_at");
        if (t2 != null) r.decidedAt = t2.toInstant();
        int db = rs.getInt("decided_by");
        r.decidedBy = rs.wasNull() ? null : db;
        r.studentName = rs.getString("student_name");
        r.className = rs.getString("class_name");
        r.invoiceCode = rs.getString("invoice_code");
        return r;
    }
}
