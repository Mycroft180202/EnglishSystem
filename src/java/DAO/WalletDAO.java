package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class WalletDAO extends DBContext {
    public BigDecimal getBalance(int studentId) throws Exception {
        String sql = "SELECT balance FROM dbo.student_wallets WHERE student_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return BigDecimal.ZERO;
                BigDecimal b = rs.getBigDecimal("balance");
                return b == null ? BigDecimal.ZERO : b;
            }
        }
    }

    public void ensureWallet(Connection con, int studentId) throws Exception {
        String sql = """
                IF NOT EXISTS (SELECT 1 FROM dbo.student_wallets WHERE student_id = ?)
                    INSERT INTO dbo.student_wallets(student_id, balance) VALUES(?, 0)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    public void topup(int studentId, BigDecimal amount, Integer createdByUserId, String note) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount");
        try (Connection con = getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                ensureWallet(con, studentId);

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dbo.student_wallets SET balance = balance + ?, updated_at = SYSUTCDATETIME() WHERE student_id = ?")) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, studentId);
                    ps.executeUpdate();
                }

                insertTxn(con, studentId, amount, "TOPUP", null, note, createdByUserId);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAuto);
            }
        }
    }

    public void debitForEnrollment(int studentId, int enrollId, BigDecimal amount, Integer createdByUserId, String note) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount");
        try (Connection con = getConnection()) {
            boolean oldAuto = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                ensureWallet(con, studentId);

                BigDecimal balance;
                try (PreparedStatement ps = con.prepareStatement("SELECT balance FROM dbo.student_wallets WITH (UPDLOCK, ROWLOCK) WHERE student_id = ?")) {
                    ps.setInt(1, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) balance = BigDecimal.ZERO;
                        else {
                            balance = rs.getBigDecimal("balance");
                            if (balance == null) balance = BigDecimal.ZERO;
                        }
                    }
                }
                if (balance.compareTo(amount) < 0) throw new IllegalStateException("INSUFFICIENT_BALANCE");

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dbo.student_wallets SET balance = balance - ?, updated_at = SYSUTCDATETIME() WHERE student_id = ?")) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, studentId);
                    ps.executeUpdate();
                }

                insertTxn(con, studentId, amount.negate(), "ENROLLMENT_FEE", enrollId, note, createdByUserId);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAuto);
            }
        }
    }

    private static void insertTxn(Connection con, int studentId, BigDecimal amount, String type, Integer enrollId, String note, Integer createdByUserId) throws Exception {
        String sql = """
                INSERT INTO dbo.wallet_transactions(student_id, amount, txn_type, enroll_id, note, created_by)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, type);
            if (enrollId == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, enrollId);
            if (note == null || note.isBlank()) ps.setNull(5, Types.NVARCHAR);
            else ps.setString(5, note.trim());
            if (createdByUserId == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, createdByUserId);
            ps.executeUpdate();
        }
    }
}

