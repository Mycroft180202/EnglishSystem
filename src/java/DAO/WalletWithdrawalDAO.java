package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class WalletWithdrawalDAO extends DBContext {
    public static final class WithdrawalRow {
        private int requestId;
        private int studentId;
        private String studentName;
        private String studentPhone;
        private BigDecimal amount;
        private String status;
        private java.time.Instant createdAt;
        private Integer createdBy;
        private java.time.Instant decidedAt;
        private Integer decidedBy;

        public int getRequestId() { return requestId; }
        public int getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getStudentPhone() { return studentPhone; }
        public BigDecimal getAmount() { return amount; }
        public String getStatus() { return status; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public Integer getCreatedBy() { return createdBy; }
        public java.time.Instant getDecidedAt() { return decidedAt; }
        public Integer getDecidedBy() { return decidedBy; }
    }

    public int create(int studentId, BigDecimal amount, String note, Integer createdByUserId) throws Exception {
        String sql = """
                INSERT INTO dbo.wallet_withdrawal_requests(student_id, amount, note, created_by)
                VALUES(?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setBigDecimal(2, amount);
            if (note == null || note.isBlank()) ps.setNull(3, Types.NVARCHAR);
            else ps.setString(3, note.trim());
            if (createdByUserId == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, createdByUserId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public List<WithdrawalRow> listAll(String statusFilter) throws Exception {
        String sql = """
                SELECT w.request_id, w.student_id, s.full_name AS student_name, s.phone AS student_phone,
                       w.amount, w.status, w.created_at, w.created_by, w.decided_at, w.decided_by
                FROM dbo.wallet_withdrawal_requests w
                JOIN dbo.students s ON w.student_id = s.student_id
                WHERE (? IS NULL OR w.status = ?)
                ORDER BY w.request_id DESC
                """;
        List<WithdrawalRow> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
        }
        return rows;
    }

    public List<WithdrawalRow> listPending() throws Exception {
        String sql = """
                SELECT w.request_id, w.student_id, s.full_name AS student_name, s.phone AS student_phone,
                       w.amount, w.status, w.created_at, w.created_by, w.decided_at, w.decided_by
                FROM dbo.wallet_withdrawal_requests w
                JOIN dbo.students s ON w.student_id = s.student_id
                WHERE w.status = N'PENDING'
                ORDER BY w.created_at ASC
                """;
        List<WithdrawalRow> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
        }
        return rows;
    }

    public WithdrawalRow findById(int requestId) throws Exception {
        String sql = """
                SELECT w.request_id, w.student_id, s.full_name AS student_name, s.phone AS student_phone,
                       w.amount, w.status, w.created_at, w.created_by, w.decided_at, w.decided_by
                FROM dbo.wallet_withdrawal_requests w
                JOIN dbo.students s ON w.student_id = s.student_id
                WHERE w.request_id = ?
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

    public boolean approveAndDebit(int requestId, Integer decidedByUserId) throws Exception {
        WalletDAO walletDAO = new WalletDAO();

        try (Connection con = getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                String lockSql = """
                        SELECT student_id, amount, status
                        FROM dbo.wallet_withdrawal_requests WITH (UPDLOCK, ROWLOCK)
                        WHERE request_id = ?
                        """;
                int studentId;
                BigDecimal amount;
                String status;
                try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) return false;
                        studentId = rs.getInt("student_id");
                        amount = rs.getBigDecimal("amount");
                        status = rs.getString("status");
                    }
                }

                if (!"PENDING".equalsIgnoreCase(status)) return false;

                walletDAO.debit(con, studentId, amount, "WITHDRAWAL", null,
                        "Cash withdrawal request #" + requestId, decidedByUserId);

                String upd = """
                        UPDATE dbo.wallet_withdrawal_requests
                        SET status = N'APPROVED', decided_at = SYSUTCDATETIME(), decided_by = ?
                        WHERE request_id = ?
                        """;
                try (PreparedStatement ps = con.prepareStatement(upd)) {
                    if (decidedByUserId == null) ps.setNull(1, Types.INTEGER);
                    else ps.setInt(1, decidedByUserId);
                    ps.setInt(2, requestId);
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAuto);
            }
        }
    }

    public boolean reject(int requestId, Integer decidedByUserId, String note) throws Exception {
        String sql = """
                UPDATE dbo.wallet_withdrawal_requests
                SET status = N'REJECTED',
                    decided_at = SYSUTCDATETIME(),
                    decided_by = ?,
                    note = COALESCE(NULLIF(?, N''), note)
                WHERE request_id = ? AND status = N'PENDING'
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (decidedByUserId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, decidedByUserId);
            ps.setString(2, note == null ? null : note.trim());
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    private static WithdrawalRow map(ResultSet rs) throws Exception {
        WithdrawalRow w = new WithdrawalRow();
        w.requestId = rs.getInt("request_id");
        w.studentId = rs.getInt("student_id");
        w.studentName = rs.getString("student_name");
        w.studentPhone = rs.getString("student_phone");
        w.amount = rs.getBigDecimal("amount");
        w.status = rs.getString("status");
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) w.createdAt = cAt.toInstant();
        int cb = rs.getInt("created_by");
        w.createdBy = rs.wasNull() ? null : cb;
        Timestamp dAt = rs.getTimestamp("decided_at");
        if (dAt != null) w.decidedAt = dAt.toInstant();
        int db = rs.getInt("decided_by");
        w.decidedBy = rs.wasNull() ? null : db;
        return w;
    }
}
