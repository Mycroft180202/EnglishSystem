package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class AbsenceRequestDAO extends DBContext {
    public boolean existsForSessionEnroll(int sessionId, int enrollId) throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.absence_requests WHERE session_id = ? AND enroll_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int create(int sessionId, int enrollId, String reason, Integer createdByUserId) throws Exception {
        String sql = """
                INSERT INTO dbo.absence_requests(session_id, enroll_id, reason, status, created_by)
                VALUES(?, ?, ?, N'PENDING', ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, enrollId);
            ps.setString(3, reason);
            if (createdByUserId == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, createdByUserId);
            return ps.executeUpdate();
        }
    }

    public void approveForSessionEnroll(int sessionId, int enrollId) throws Exception {
        String sql = "UPDATE dbo.absence_requests SET status = N'APPROVED' WHERE session_id = ? AND enroll_id = ? AND status = N'PENDING'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, enrollId);
            ps.executeUpdate();
        }
    }

    public void approveAllPendingForSession(int sessionId) throws Exception {
        String sql = "UPDATE dbo.absence_requests SET status = N'APPROVED' WHERE session_id = ? AND status = N'PENDING'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        }
    }
}
