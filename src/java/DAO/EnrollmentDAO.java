package DAO;

import Model.Enrollment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO extends DBContext {
    public static final class RefundResult {
        public boolean ok;
        public boolean refunded;
        public String message;

        public static RefundResult ok(boolean refunded, String message) {
            RefundResult r = new RefundResult();
            r.ok = true;
            r.refunded = refunded;
            r.message = message;
            return r;
        }

        public static RefundResult fail(String message) {
            RefundResult r = new RefundResult();
            r.ok = false;
            r.refunded = false;
            r.message = message;
            return r;
        }
    }

    public static final class EnrollPayInfo {
        private int enrollId;
        private int studentId;
        private int classId;
        private java.math.BigDecimal standardFee;
        private String enrollStatus;
        private Integer invoiceId;
        private String invoiceStatus;

        public int getEnrollId() { return enrollId; }
        public int getStudentId() { return studentId; }
        public int getClassId() { return classId; }
        public java.math.BigDecimal getStandardFee() { return standardFee; }
        public String getEnrollStatus() { return enrollStatus; }
        public Integer getInvoiceId() { return invoiceId; }
        public String getInvoiceStatus() { return invoiceStatus; }
    }

    public List<Enrollment> listAll(Integer classId, Integer studentId, String status) throws Exception {
        String sql = """
                SELECT e.enroll_id, e.student_id, e.class_id, e.enrolled_at, e.status,
                       i.invoice_id, i.status AS invoice_status,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_code, c.class_name, cr.course_name, cr.status AS course_status,
                       c.start_date, c.end_date,
                       CASE WHEN EXISTS (SELECT 1 FROM dbo.class_schedules cs WHERE cs.class_id = c.class_id) THEN 1 ELSE 0 END AS has_schedule
                FROM dbo.enrollments e
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.invoices i ON i.enroll_id = e.enroll_id
                WHERE (? IS NULL OR e.class_id = ?)
                  AND (? IS NULL OR e.student_id = ?)
                  AND (? IS NULL OR e.status = ?)
                ORDER BY e.enroll_id ASC
                """;
        List<Enrollment> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, classId);
            ps.setObject(2, classId);
            ps.setObject(3, studentId);
            ps.setObject(4, studentId);
            ps.setString(5, status);
            ps.setString(6, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public int countActiveByClass(int classId) throws Exception {
        // Count seats that should block new registrations (ACTIVE + PENDING).
        String sql = "SELECT COUNT(*) AS cnt FROM dbo.enrollments WHERE class_id = ? AND status IN (N'ACTIVE', N'PENDING')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt");
            }
        }
    }

    public int getClassCapacity(int classId) throws Exception {
        String sql = "SELECT capacity FROM dbo.classes WHERE class_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                return rs.getInt("capacity");
            }
        }
    }

    public boolean hasScheduleConflict(int studentId, int newClassId) throws Exception {
        /*
          Conflict when any ACTIVE/PENDING enrollment for student has a class schedule overlapping day_of_week + slot_id
          with the new class schedule.
        */
        String sql = """
                SELECT TOP 1 1
                FROM dbo.enrollments e
                JOIN dbo.class_schedules cs1 ON e.class_id = cs1.class_id
                JOIN dbo.class_schedules cs2 ON cs2.class_id = ?
                   AND cs2.day_of_week = cs1.day_of_week
                   AND cs2.slot_id = cs1.slot_id
                WHERE e.student_id = ?
                  AND e.status IN (N'ACTIVE', N'PENDING')
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newClassId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer findEnrollIdForStudentAndSession(int studentId, int sessionId) throws Exception {
        String sql = """
                SELECT TOP 1 e.enroll_id
                FROM dbo.class_sessions cs
                JOIN dbo.enrollments e ON cs.class_id = e.class_id
                WHERE cs.session_id = ?
                  AND e.student_id = ?
                  AND e.status IN (N'ACTIVE', N'COMPLETED')
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("enroll_id");
            }
        }
    }

    public int create(int studentId, int classId, String status) throws Exception {
        String sql = """
                INSERT INTO dbo.enrollments(student_id, class_id, status)
                VALUES(?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public List<Enrollment> listActiveByClass(int classId) throws Exception {
        String sql = """
                SELECT e.enroll_id, e.student_id, e.class_id, e.enrolled_at, e.status,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_code, c.class_name, cr.course_name
                FROM dbo.enrollments e
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE e.class_id = ? AND e.status = N'ACTIVE'
                ORDER BY s.full_name
                """;
        List<Enrollment> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public void setStatus(int enrollId, String status) throws Exception {
        String sql = "UPDATE dbo.enrollments SET status = ? WHERE enroll_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, enrollId);
            ps.executeUpdate();
        }
    }

    private static void setStatus(Connection con, int enrollId, String status) throws Exception {
        String sql = "UPDATE dbo.enrollments SET status = ? WHERE enroll_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, enrollId);
            ps.executeUpdate();
        }
    }

    public RefundResult refundToWalletIfNeeded(int enrollId, Integer actorUserId) throws Exception {
        try (Connection con = getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                RefundResult r = refundToWalletIfNeeded(con, enrollId, actorUserId);
                if (!r.ok) {
                    con.rollback();
                    return r;
                }
                con.commit();
                return r;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAuto);
            }
        }
    }

    public RefundResult cancelWithRefundToWallet(int enrollId, Integer actorUserId) throws Exception {
        try (Connection con = getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                RefundResult r = refundToWalletIfNeeded(con, enrollId, actorUserId);
                if (!r.ok) {
                    con.rollback();
                    return r;
                }
                setStatus(con, enrollId, "CANCELLED");
                con.commit();
                if (r.refunded) return RefundResult.ok(true, "Đã hủy đăng ký và hoàn tiền về ví.");
                return RefundResult.ok(false, "Đã hủy đăng ký.");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAuto);
            }
        }
    }

    private RefundResult refundToWalletIfNeeded(Connection con, int enrollId, Integer actorUserId) throws Exception {
        String sql = """
                SELECT e.student_id, e.class_id,
                       i.invoice_id, i.status AS invoice_status, i.paid_amount
                FROM dbo.enrollments e
                LEFT JOIN dbo.invoices i WITH (UPDLOCK, ROWLOCK) ON i.enroll_id = e.enroll_id
                WHERE e.enroll_id = ?
                """;

        int studentId;
        int classId;
        Integer invoiceId;
        String invoiceStatus;
        BigDecimal paidAmount;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return RefundResult.fail("Không tìm thấy đăng ký để hoàn tiền.");
                studentId = rs.getInt("student_id");
                classId = rs.getInt("class_id");
                int inv = rs.getInt("invoice_id");
                invoiceId = rs.wasNull() ? null : inv;
                invoiceStatus = rs.getString("invoice_status");
                paidAmount = rs.getBigDecimal("paid_amount");
            }
        }

        // Refund rule: if learned > 20% sessions, do not refund.
        Progress p = getClassProgress(con, classId);
        if (p.totalSessions > 0 && (p.completedSessions * 5L) > p.totalSessions) {
            return RefundResult.fail("Học viên đã học hơn 20% số buổi nên không được hoàn tiền.");
        }

        if (invoiceId == null || paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return RefundResult.ok(false, "Không có khoản thanh toán để hoàn tiền.");
        }
        if ("VOID".equalsIgnoreCase(invoiceStatus)) {
            return RefundResult.ok(false, "Hóa đơn đã VOID (đã hoàn hoặc hủy trước đó).");
        }
        if (refundTxnExists(con, enrollId)) {
            return RefundResult.ok(false, "Đã hoàn tiền trước đó.");
        }

        WalletDAO walletDAO = new WalletDAO();
        InvoiceDAO invoiceDAO = new InvoiceDAO();

        walletDAO.credit(con, studentId, paidAmount, "REFUND", enrollId,
                "Refund for enrollment #" + enrollId + ", invoice #" + invoiceId,
                actorUserId);

        invoiceDAO.updatePaidAmountAndStatus(con, invoiceId, BigDecimal.ZERO, "VOID");

        return RefundResult.ok(true, "Đã hoàn tiền về ví.");
    }

    private static final class Progress {
        int totalSessions;
        int completedSessions;
    }

    private static Progress getClassProgress(Connection con, int classId) throws Exception {
        Progress p = new Progress();
        String totalSql = "SELECT COUNT(*) AS cnt FROM dbo.class_sessions WHERE class_id = ? AND status <> N'CANCELLED'";
        String doneSql = "SELECT COUNT(*) AS cnt FROM dbo.class_sessions WHERE class_id = ? AND status = N'COMPLETED'";
        try (PreparedStatement ps = con.prepareStatement(totalSql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.totalSessions = rs.getInt("cnt");
            }
        }
        try (PreparedStatement ps = con.prepareStatement(doneSql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.completedSessions = rs.getInt("cnt");
            }
        }
        return p;
    }

    private static boolean refundTxnExists(Connection con, int enrollId) throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.wallet_transactions WHERE enroll_id = ? AND txn_type = N'REFUND'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public EnrollPayInfo findPayInfo(int enrollId) throws Exception {
        String sql = """
                SELECT e.enroll_id, e.student_id, e.class_id, e.status AS enroll_status,
                       cr.standard_fee,
                       i.invoice_id, i.status AS invoice_status
                FROM dbo.enrollments e
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.invoices i ON i.enroll_id = e.enroll_id
                WHERE e.enroll_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                EnrollPayInfo info = new EnrollPayInfo();
                info.enrollId = rs.getInt("enroll_id");
                info.studentId = rs.getInt("student_id");
                info.classId = rs.getInt("class_id");
                info.enrollStatus = rs.getString("enroll_status");
                info.standardFee = rs.getBigDecimal("standard_fee");
                int invoiceId = rs.getInt("invoice_id");
                info.invoiceId = rs.wasNull() ? null : invoiceId;
                info.invoiceStatus = rs.getString("invoice_status");
                return info;
            }
        }
    }

    private static Enrollment map(ResultSet rs) throws Exception {
        Enrollment e = new Enrollment();
        e.setEnrollId(rs.getInt("enroll_id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setClassId(rs.getInt("class_id"));
        Timestamp t = rs.getTimestamp("enrolled_at");
        if (t != null) e.setEnrolledAt(t.toInstant());
        e.setStatus(rs.getString("status"));
        try {
            Date sd = rs.getDate("start_date");
            if (sd != null) e.setClassStartDate(sd.toLocalDate());
        } catch (Exception ignored) {}
        try {
            Date ed = rs.getDate("end_date");
            if (ed != null) e.setClassEndDate(ed.toLocalDate());
        } catch (Exception ignored) {}
        try {
            e.setHasSchedule(rs.getInt("has_schedule") == 1);
        } catch (Exception ignored) {}
        try {
            int inv = rs.getInt("invoice_id");
            e.setInvoiceId(rs.wasNull() ? null : inv);
        } catch (Exception ignored) {}
        try { e.setInvoiceStatus(rs.getString("invoice_status")); } catch (Exception ignored) {}
        e.setStudentName(rs.getString("student_name"));
        e.setStudentPhone(rs.getString("student_phone"));
        e.setClassCode(rs.getString("class_code"));
        e.setClassName(rs.getString("class_name"));
        e.setCourseName(rs.getString("course_name"));
        try { e.setCourseStatus(rs.getString("course_status")); } catch (Exception ignored) {}
        return e;
    }

    public DeleteResult deleteCascade(int enrollId) throws Exception {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                exec(con, "DELETE FROM dbo.attendance WHERE enroll_id = ?", enrollId);
                exec(con, "DELETE FROM dbo.absence_requests WHERE enroll_id = ?", enrollId);
                exec(con, "DELETE FROM dbo.scores WHERE enroll_id = ?", enrollId);

                exec(con, """
                        DELETE p
                        FROM dbo.payments p
                        JOIN dbo.invoices i ON p.invoice_id = i.invoice_id
                        WHERE i.enroll_id = ?
                        """, enrollId);
                exec(con, "DELETE FROM dbo.payment_requests WHERE enroll_id = ?", enrollId);
                exec(con, "DELETE FROM dbo.vietqr_payment_intents WHERE enroll_id = ?", enrollId);
                exec(con, "DELETE FROM dbo.payos_payment_intents WHERE enroll_id = ?", enrollId);
                exec(con, "DELETE FROM dbo.invoices WHERE enroll_id = ?", enrollId);
                // Keep wallet ledger, but detach from enroll_id to satisfy FK before deleting enrollment.
                exec(con, "UPDATE dbo.wallet_transactions SET enroll_id = NULL WHERE enroll_id = ?", enrollId);

                int deleted = exec(con, "DELETE FROM dbo.enrollments WHERE enroll_id = ?", enrollId);
                con.commit();
                if (deleted <= 0) return DeleteResult.fail("Không tìm thấy đăng ký để xóa.");
                return DeleteResult.ok("Đã xóa đăng ký (kèm hóa đơn/thu tiền/điểm danh/điểm).");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static int exec(Connection con, String sql, int id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
