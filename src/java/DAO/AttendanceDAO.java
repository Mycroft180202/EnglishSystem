package DAO;

import Model.AttendanceRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceDAO extends DBContext {
    public boolean sessionBelongsToTeacher(int sessionId, int teacherId) throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.class_sessions WHERE session_id = ? AND teacher_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<AttendanceRow> listForSession(int sessionId) throws Exception {
        /*
          List all ACTIVE enrollments of the class for this session, with existing attendance if any.
        */
        String sql = """
                SELECT e.enroll_id, e.student_id, s.full_name AS student_name, s.phone AS student_phone,
                       a.status AS att_status, a.note AS att_note
                FROM dbo.class_sessions cs
                JOIN dbo.enrollments e ON cs.class_id = e.class_id AND e.status = N'ACTIVE'
                JOIN dbo.students s ON e.student_id = s.student_id
                LEFT JOIN dbo.attendance a ON a.session_id = cs.session_id AND a.enroll_id = e.enroll_id
                WHERE cs.session_id = ?
                ORDER BY s.full_name
                """;
        List<AttendanceRow> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRow r = new AttendanceRow();
                    r.setEnrollId(rs.getInt("enroll_id"));
                    r.setStudentId(rs.getInt("student_id"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setStudentPhone(rs.getString("student_phone"));
                    r.setStatus(rs.getString("att_status"));
                    r.setNote(rs.getString("att_note"));
                    result.add(r);
                }
            }
        }
        return result;
    }

    public void upsertForSession(int sessionId, Integer markedByUserId, Map<Integer, AttendanceRow> rows) throws Exception {
        if (rows == null || rows.isEmpty()) return;

        String mergeSql = """
                MERGE dbo.attendance AS target
                USING (SELECT ? AS session_id, ? AS enroll_id) AS source
                ON (target.session_id = source.session_id AND target.enroll_id = source.enroll_id)
                WHEN MATCHED THEN
                    UPDATE SET status = ?, note = ?, marked_at = SYSUTCDATETIME(), marked_by = ?
                WHEN NOT MATCHED THEN
                    INSERT (session_id, enroll_id, status, note, marked_at, marked_by)
                    VALUES (?, ?, ?, ?, SYSUTCDATETIME(), ?);
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(mergeSql)) {
            for (AttendanceRow r : rows.values()) {
                ps.setInt(1, sessionId);
                ps.setInt(2, r.getEnrollId());
                ps.setString(3, r.getStatus());
                if (r.getNote() == null || r.getNote().isBlank()) ps.setNull(4, Types.NVARCHAR);
                else ps.setString(4, r.getNote().trim());
                if (markedByUserId == null) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, markedByUserId);

                ps.setInt(6, sessionId);
                ps.setInt(7, r.getEnrollId());
                ps.setString(8, r.getStatus());
                if (r.getNote() == null || r.getNote().isBlank()) ps.setNull(9, Types.NVARCHAR);
                else ps.setString(9, r.getNote().trim());
                if (markedByUserId == null) ps.setNull(10, Types.INTEGER);
                else ps.setInt(10, markedByUserId);

                ps.executeUpdate();
            }
        }
    }
}

