package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SessionAssessmentDAO extends DBContext {
    public void assign(int sessionId, int classId, int assessId, Integer assignedByUserId) throws Exception {
        String deleteBySession = "DELETE FROM dbo.session_assessments WHERE session_id = ?";
        String updateExisting = """
                UPDATE dbo.session_assessments
                SET session_id = ?, assigned_at = SYSUTCDATETIME(), assigned_by = ?
                WHERE class_id = ? AND assess_id = ?
                """;
        String insertNew = """
                INSERT INTO dbo.session_assessments(session_id, class_id, assess_id, assigned_at, assigned_by)
                VALUES(?, ?, ?, SYSUTCDATETIME(), ?)
                """;
        String existsSql = "SELECT 1 FROM dbo.session_assessments WHERE class_id = ? AND assess_id = ?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(deleteBySession)) {
                    ps.setInt(1, sessionId);
                    ps.executeUpdate();
                }

                boolean exists;
                try (PreparedStatement ps = con.prepareStatement(existsSql)) {
                    ps.setInt(1, classId);
                    ps.setInt(2, assessId);
                    exists = ps.executeQuery().next();
                }

                if (exists) {
                    try (PreparedStatement ps = con.prepareStatement(updateExisting)) {
                        ps.setInt(1, sessionId);
                        if (assignedByUserId == null) ps.setNull(2, java.sql.Types.INTEGER);
                        else ps.setInt(2, assignedByUserId);
                        ps.setInt(3, classId);
                        ps.setInt(4, assessId);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(insertNew)) {
                        ps.setInt(1, sessionId);
                        ps.setInt(2, classId);
                        ps.setInt(3, assessId);
                        if (assignedByUserId == null) ps.setNull(4, java.sql.Types.INTEGER);
                        else ps.setInt(4, assignedByUserId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (Exception ex) {
                try { con.rollback(); } catch (Exception ignore) {}
                throw ex;
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    public void clear(int sessionId) throws Exception {
        String sql = "DELETE FROM dbo.session_assessments WHERE session_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        }
    }
}

