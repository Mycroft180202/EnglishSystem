package DAO;

import Model.CenterClass;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO extends DBContext {
    public static final class DateRange {
        public final LocalDate startDate;
        public final LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public List<CenterClass> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name, cr.standard_fee,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name,
                       (SELECT COUNT(*) FROM dbo.enrollments e WHERE e.class_id = c.class_id AND e.status = N'ACTIVE') AS active_enroll_count
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                LEFT JOIN dbo.rooms r ON c.room_id = r.room_id
                WHERE (? IS NULL OR c.status = ?)
                  AND (? IS NULL OR c.class_code LIKE ? OR c.class_name LIKE ? OR cr.course_name LIKE ? OR t.full_name LIKE ? OR r.room_name LIKE ?)
                ORDER BY c.class_id ASC
                """;
        List<CenterClass> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = q == null ? null : "%" + q + "%";
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            ps.setString(8, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<CenterClass> listOpenForStudent(String q) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name, cr.standard_fee,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name,
                       (SELECT COUNT(*) FROM dbo.enrollments e WHERE e.class_id = c.class_id AND e.status = N'ACTIVE') AS active_enroll_count
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                LEFT JOIN dbo.rooms r ON c.room_id = r.room_id
                WHERE c.status = N'OPEN'
                  AND c.start_date IS NOT NULL
                  AND EXISTS (SELECT 1 FROM dbo.class_schedules cs WHERE cs.class_id = c.class_id)
                  AND (? IS NULL OR c.class_code LIKE ? OR c.class_name LIKE ? OR cr.course_name LIKE ?)
                ORDER BY c.class_id ASC
                """;
        List<CenterClass> result = new ArrayList<>();
        String like = q == null ? null : "%" + q + "%";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public DateRange resolveDateRange(int classId) throws Exception {
        String sql = """
                SELECT c.start_date, c.end_date, cr.duration_weeks
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE c.class_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Date startD = rs.getDate("start_date");
                Date endD = rs.getDate("end_date");
                if (startD == null) return null;
                LocalDate start = startD.toLocalDate();
                LocalDate end;
                if (endD != null) {
                    end = endD.toLocalDate();
                } else {
                    int weeks = rs.getInt("duration_weeks");
                    if (weeks <= 0) weeks = 1;
                    end = start.plusDays((long) weeks * 7L - 1L);
                }
                return new DateRange(start, end);
            }
        }
    }

    public CenterClass findById(int classId) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name, cr.standard_fee,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name,
                       (SELECT COUNT(*) FROM dbo.enrollments e WHERE e.class_id = c.class_id AND e.status = N'ACTIVE') AS active_enroll_count
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                LEFT JOIN dbo.rooms r ON c.room_id = r.room_id
                WHERE c.class_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public CenterClass findByCode(String classCode) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name, cr.standard_fee,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name,
                       (SELECT COUNT(*) FROM dbo.enrollments e WHERE e.class_id = c.class_id AND e.status = N'ACTIVE') AS active_enroll_count
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                LEFT JOIN dbo.rooms r ON c.room_id = r.room_id
                WHERE c.class_code = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, classCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(CenterClass c) throws Exception {
        String sql = """
                INSERT INTO dbo.classes(course_id, class_code, class_name, teacher_id, room_id, capacity, start_date, end_date, status)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, c, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(CenterClass c) throws Exception {
        String sql = """
                UPDATE dbo.classes
                SET course_id = ?, class_code = ?, class_name = ?, teacher_id = ?, room_id = ?, capacity = ?,
                    start_date = ?, end_date = ?, status = ?
                WHERE class_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, c, true);
            ps.executeUpdate();
        }
    }

    public void setStatus(int classId, String status) throws Exception {
        String sql = "UPDATE dbo.classes SET status = ? WHERE class_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, classId);
            ps.executeUpdate();
        }
    }

    /**
     * Sync dbo.classes.room_id based on dbo.class_schedules.
     * - If the class has exactly 1 distinct room in schedules => set that as default room_id
     * - Otherwise (0 or >1) => set room_id = NULL
     */
    public void syncRoomFromSchedules(int classId) throws Exception {
        String cntSql = "SELECT COUNT(DISTINCT room_id) AS cnt FROM dbo.class_schedules WHERE class_id = ?";
        String roomSql = "SELECT MIN(room_id) AS room_id FROM dbo.class_schedules WHERE class_id = ?";
        try (Connection con = getConnection()) {
            int cnt;
            try (PreparedStatement ps = con.prepareStatement(cntSql)) {
                ps.setInt(1, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    cnt = rs.getInt("cnt");
                }
            }

            Integer roomId = null;
            if (cnt == 1) {
                try (PreparedStatement ps = con.prepareStatement(roomSql)) {
                    ps.setInt(1, classId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int r = rs.getInt("room_id");
                            roomId = rs.wasNull() ? null : r;
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.classes SET room_id = ? WHERE class_id = ?")) {
                if (roomId == null) ps.setNull(1, java.sql.Types.INTEGER);
                else ps.setInt(1, roomId);
                ps.setInt(2, classId);
                ps.executeUpdate();
            }
        }
    }

    private static CenterClass map(ResultSet rs) throws Exception {
        CenterClass c = new CenterClass();
        c.setClassId(rs.getInt("class_id"));
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseName(rs.getString("course_name"));
        try { c.setStandardFee(rs.getBigDecimal("standard_fee")); } catch (Exception ignored) {}
        c.setClassCode(rs.getString("class_code"));
        c.setClassName(rs.getString("class_name"));

        int teacherId = rs.getInt("teacher_id");
        c.setTeacherId(rs.wasNull() ? null : teacherId);
        c.setTeacherName(rs.getString("teacher_name"));

        int roomId = rs.getInt("room_id");
        c.setRoomId(rs.wasNull() ? null : roomId);
        c.setRoomName(rs.getString("room_name"));

        c.setCapacity(rs.getInt("capacity"));
        Date start = rs.getDate("start_date");
        if (start != null) c.setStartDate(start.toLocalDate());
        Date end = rs.getDate("end_date");
        if (end != null) c.setEndDate(end.toLocalDate());
        c.setStatus(rs.getString("status"));
        try {
            int cnt = rs.getInt("active_enroll_count");
            c.setActiveEnrollCount(rs.wasNull() ? null : cnt);
        } catch (Exception ignored) {}
        return c;
    }

    private static void bind(PreparedStatement ps, CenterClass c, boolean includeIdAtEnd) throws Exception {
        ps.setInt(1, c.getCourseId());
        ps.setString(2, c.getClassCode());
        ps.setString(3, c.getClassName());
        if (c.getTeacherId() == null) ps.setNull(4, java.sql.Types.INTEGER);
        else ps.setInt(4, c.getTeacherId());
        if (c.getRoomId() == null) ps.setNull(5, java.sql.Types.INTEGER);
        else ps.setInt(5, c.getRoomId());
        ps.setInt(6, c.getCapacity());
        ps.setDate(7, c.getStartDate() == null ? null : Date.valueOf(c.getStartDate()));
        ps.setDate(8, c.getEndDate() == null ? null : Date.valueOf(c.getEndDate()));
        ps.setString(9, c.getStatus());
        if (includeIdAtEnd) ps.setInt(10, c.getClassId());
    }

    public DeleteResult deleteCascade(int classId) throws Exception {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // Remove session_assessments first (FK to class_sessions/assessments).
                exec(con, "DELETE FROM dbo.session_assessments WHERE class_id = ?", classId);

                // Attendance / absence / scores for enrollments in this class
                exec(con, """
                        DELETE a
                        FROM dbo.attendance a
                        JOIN dbo.enrollments e ON a.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE ar
                        FROM dbo.absence_requests ar
                        JOIN dbo.enrollments e ON ar.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE sc
                        FROM dbo.scores sc
                        JOIN dbo.enrollments e ON sc.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);

                // Payment-related tables (invoice -> enrollments -> class)
                exec(con, """
                        DELETE p
                        FROM dbo.payments p
                        JOIN dbo.invoices i ON p.invoice_id = i.invoice_id
                        JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE pr
                        FROM dbo.payment_requests pr
                        JOIN dbo.enrollments e ON pr.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE v
                        FROM dbo.vietqr_payment_intents v
                        JOIN dbo.enrollments e ON v.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE po
                        FROM dbo.payos_payment_intents po
                        JOIN dbo.enrollments e ON po.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);
                exec(con, """
                        DELETE i
                        FROM dbo.invoices i
                        JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);

                exec(con, """
                        DELETE wt
                        FROM dbo.wallet_transactions wt
                        JOIN dbo.enrollments e ON wt.enroll_id = e.enroll_id
                        WHERE e.class_id = ?
                        """, classId);

                exec(con, "DELETE FROM dbo.enrollments WHERE class_id = ?", classId);

                exec(con, "DELETE FROM dbo.class_sessions WHERE class_id = ?", classId);
                exec(con, "DELETE FROM dbo.class_schedules WHERE class_id = ?", classId);

                int deleted = exec(con, "DELETE FROM dbo.classes WHERE class_id = ?", classId);
                con.commit();
                if (deleted <= 0) return DeleteResult.fail("Không tìm thấy lớp để xóa.");
                return DeleteResult.ok("Đã xóa lớp (kèm dữ liệu liên quan: lịch, buổi học, đăng ký, hóa đơn...).");
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
