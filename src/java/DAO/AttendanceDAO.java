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
    public static final class EnrollStats {
        private int enrollId;
        private int totalSessions;
        private int attended;
        private int absent;
        private int excused;

        public int getEnrollId() {
            return enrollId;
        }

        public void setEnrollId(int enrollId) {
            this.enrollId = enrollId;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(int totalSessions) {
            this.totalSessions = totalSessions;
        }

        public int getAttended() {
            return attended;
        }

        public void setAttended(int attended) {
            this.attended = attended;
        }

        public int getAbsent() {
            return absent;
        }

        public void setAbsent(int absent) {
            this.absent = absent;
        }

        public int getExcused() {
            return excused;
        }

        public void setExcused(int excused) {
            this.excused = excused;
        }
    }

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
                       a.status AS att_status, a.note AS att_note,
                       ISNULL(st.attended_cnt, 0) AS attended_cnt,
                       ISNULL(st.absent_cnt, 0) AS absent_cnt,
                       ISNULL(st.excused_cnt, 0) AS excused_cnt,
                       ISNULL(ts.total_cnt, 0) AS total_cnt,
                       ar.status AS req_status,
                       ar.reason AS req_reason,
                       FORMAT(ar.created_at, 'dd/MM/yyyy HH:mm:ss') AS req_created_at
                FROM dbo.class_sessions cs
                JOIN dbo.enrollments e ON cs.class_id = e.class_id AND e.status = N'ACTIVE'
                JOIN dbo.students s ON e.student_id = s.student_id
                LEFT JOIN dbo.attendance a ON a.session_id = cs.session_id AND a.enroll_id = e.enroll_id
                OUTER APPLY (
                    SELECT
                        SUM(CASE WHEN att.status = N'ATTENDED' THEN 1 ELSE 0 END) AS attended_cnt,
                        SUM(CASE WHEN att.status = N'ABSENT' THEN 1 ELSE 0 END) AS absent_cnt,
                        SUM(CASE WHEN att.status = N'EXCUSED' THEN 1 ELSE 0 END) AS excused_cnt
                    FROM dbo.attendance att
                    JOIN dbo.class_sessions s2 ON s2.session_id = att.session_id
                    WHERE att.enroll_id = e.enroll_id
                      AND s2.session_date <= cs.session_date
                      AND s2.session_id <> cs.session_id
                ) st
                OUTER APPLY (
                    SELECT COUNT(*) AS total_cnt
                    FROM dbo.class_sessions s2
                    WHERE s2.class_id = cs.class_id
                      AND s2.session_date <= cs.session_date
                ) ts
                LEFT JOIN dbo.absence_requests ar
                    ON ar.session_id = cs.session_id AND ar.enroll_id = e.enroll_id AND ar.status IN (N'PENDING', N'APPROVED')
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
                    r.setAttendedCount(rs.getInt("attended_cnt"));
                    r.setAbsentCount(rs.getInt("absent_cnt"));
                    r.setExcusedCount(rs.getInt("excused_cnt"));
                    r.setTotalSessionsToDate(rs.getInt("total_cnt"));
                    r.setRequestStatus(rs.getString("req_status"));
                    r.setRequestReason(rs.getString("req_reason"));
                    r.setRequestCreatedAt(rs.getString("req_created_at"));
                    result.add(r);
                }
            }
        }
        return result;
    }

    public int countExcusedByEnroll(int enrollId) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM dbo.attendance WHERE enroll_id = ? AND status = N'EXCUSED'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt");
            }
        }
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

    public List<EnrollStats> statsByStudent(int studentId) throws Exception {
        String sql = """
                SELECT e.enroll_id,
                       COUNT(cs.session_id) AS total_sessions,
                       SUM(CASE WHEN a.status = N'ATTENDED' THEN 1 ELSE 0 END) AS attended_cnt,
                       SUM(CASE WHEN a.status = N'ABSENT' THEN 1 ELSE 0 END) AS absent_cnt,
                       SUM(CASE WHEN a.status = N'EXCUSED' THEN 1 ELSE 0 END) AS excused_cnt
                FROM dbo.enrollments e
                JOIN dbo.class_sessions cs ON cs.class_id = e.class_id
                LEFT JOIN dbo.attendance a ON a.session_id = cs.session_id AND a.enroll_id = e.enroll_id
                WHERE e.student_id = ?
                  AND e.status IN (N'ACTIVE', N'COMPLETED')
                GROUP BY e.enroll_id
                """;
        List<EnrollStats> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EnrollStats st = new EnrollStats();
                    st.setEnrollId(rs.getInt("enroll_id"));
                    st.setTotalSessions(rs.getInt("total_sessions"));
                    st.setAttended(rs.getInt("attended_cnt"));
                    st.setAbsent(rs.getInt("absent_cnt"));
                    st.setExcused(rs.getInt("excused_cnt"));
                    result.add(st);
                }
            }
        }
        return result;
    }
}
