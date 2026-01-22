package DAO;

import Model.ClassSchedule;
import Model.ClassSession;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class ClassSessionDAO extends DBContext {
    public ClassSession findByIdExtended(int sessionId) throws Exception {
        String sql = """
                SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, s.teacher_id, s.status,
                       FORMAT(s.teacher_checkin_at, 'dd/MM/yyyy HH:mm:ss') AS teacher_checkin_at,
                       ROW_NUMBER() OVER (PARTITION BY s.class_id ORDER BY s.session_date, ts.start_time, s.session_id) AS session_no,
                       c.class_code, c.class_name, cr.course_id, cr.course_name,
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name, r.room_code,
                       t.full_name AS teacher_name,
                       a.assess_id, a.type AS assess_type, a.name AS assess_name
                FROM dbo.class_sessions s
                JOIN dbo.classes c ON s.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                JOIN dbo.rooms r ON s.room_id = r.room_id
                LEFT JOIN dbo.teachers t ON s.teacher_id = t.teacher_id
                LEFT JOIN dbo.session_assessments sa ON sa.session_id = s.session_id
                LEFT JOIN dbo.assessments a ON a.assess_id = sa.assess_id
                WHERE s.session_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapExtended(rs);
            }
        }
    }

    public List<ClassSession> listByClass(int classId) throws Exception {
        String sql = """
                SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, s.teacher_id, s.status,
                       ROW_NUMBER() OVER (PARTITION BY s.class_id ORDER BY s.session_date, ts.start_time, s.session_id) AS session_no,
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name, t.full_name AS teacher_name,
                       FORMAT(s.teacher_checkin_at, 'dd/MM/yyyy HH:mm:ss') AS teacher_checkin_at
                FROM dbo.class_sessions s
                JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                JOIN dbo.rooms r ON s.room_id = r.room_id
                JOIN dbo.teachers t ON s.teacher_id = t.teacher_id
                WHERE s.class_id = ?
                ORDER BY s.session_date, ts.start_time
                """;
        List<ClassSession> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Integer findClassIdBySessionId(int sessionId) throws Exception {
        String sql = "SELECT class_id FROM dbo.class_sessions WHERE session_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("class_id");
            }
        }
    }

    public boolean hasAttendance(int sessionId) throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.attendance WHERE session_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasAbsenceRequest(int sessionId) throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.absence_requests WHERE session_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String cancelSession(int sessionId) throws Exception {
        String statusSql = "SELECT status, session_date FROM dbo.class_sessions WHERE session_id = ?";
        try (Connection con = getConnection()) {
            String status;
            LocalDate date;
            try (PreparedStatement ps = con.prepareStatement(statusSql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return "Buổi học không tồn tại.";
                    status = rs.getString("status");
                    Date d = rs.getDate("session_date");
                    date = d == null ? null : d.toLocalDate();
                }
            }

            if (!"SCHEDULED".equalsIgnoreCase(status)) return "Chỉ hủy được buổi ở trạng thái SCHEDULED.";
            if (date != null && date.isBefore(LocalDate.now())) return "Không thể hủy buổi đã diễn ra.";
            if (hasAttendance(sessionId)) return "Không thể hủy: đã có điểm danh.";

            try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.class_sessions SET status = N'CANCELLED' WHERE session_id = ?")) {
                ps.setInt(1, sessionId);
                ps.executeUpdate();
            }
            return null;
        }
    }

    public String rescheduleSession(int sessionId, LocalDate newDate, int newSlotId, int newRoomId) throws Exception {
        if (newDate == null) return "Vui lòng chọn ngày.";
        if (newSlotId <= 0) return "Vui lòng chọn ca.";
        if (newRoomId <= 0) return "Vui lòng chọn phòng.";

        String infoSql = "SELECT class_id, teacher_id, status, session_date FROM dbo.class_sessions WHERE session_id = ?";
        try (Connection con = getConnection()) {
            int classId;
            int teacherId;
            String status;
            LocalDate oldDate;
            try (PreparedStatement ps = con.prepareStatement(infoSql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return "Buổi học không tồn tại.";
                    classId = rs.getInt("class_id");
                    teacherId = rs.getInt("teacher_id");
                    status = rs.getString("status");
                    Date d = rs.getDate("session_date");
                    oldDate = d == null ? null : d.toLocalDate();
                }
            }

            boolean isScheduled = "SCHEDULED".equalsIgnoreCase(status);
            boolean isCancelled = "CANCELLED".equalsIgnoreCase(status);
            if (!isScheduled && !isCancelled) return "Chỉ đổi lịch được buổi ở trạng thái SCHEDULED/CANCELLED.";
            if (isScheduled && oldDate != null && oldDate.isBefore(LocalDate.now())) return "Không thể đổi lịch buổi đã diễn ra.";
            if (hasAttendance(sessionId)) return "Không thể đổi lịch: đã có điểm danh.";

            // Prevent collision within same class (unique constraint)
            if (exists(con, """
                    SELECT 1 FROM dbo.class_sessions
                    WHERE class_id = ?
                      AND session_date = ?
                      AND slot_id = ?
                      AND session_id <> ?
                    """, classId, newDate, newSlotId, sessionId)) {
                return "Lớp đã có buổi ở ngày/ca này.";
            }

            // Room conflict (other scheduled sessions)
            if (exists(con, """
                    SELECT 1 FROM dbo.class_sessions
                    WHERE session_date = ?
                      AND slot_id = ?
                      AND room_id = ?
                      AND status = N'SCHEDULED'
                      AND session_id <> ?
                    """, newDate, newSlotId, newRoomId, sessionId)) {
                return "Phòng đã có lớp khác sử dụng ở ngày/ca này.";
            }

            // Teacher conflict (other scheduled sessions)
            if (exists(con, """
                    SELECT 1 FROM dbo.class_sessions
                    WHERE session_date = ?
                      AND slot_id = ?
                      AND teacher_id = ?
                      AND status = N'SCHEDULED'
                      AND session_id <> ?
                    """, newDate, newSlotId, teacherId, sessionId)) {
                return "Giáo viên bị trùng lịch ở ngày/ca này.";
            }

            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE dbo.class_sessions
                    SET session_date = ?, slot_id = ?, room_id = ?, status = N'SCHEDULED'
                    WHERE session_id = ?
                    """)) {
                ps.setDate(1, Date.valueOf(newDate));
                ps.setInt(2, newSlotId);
                ps.setInt(3, newRoomId);
                ps.setInt(4, sessionId);
                ps.executeUpdate();
            }

            return null;
        }
    }

    public String markTeacherCheckIn(int sessionId, int userId) throws Exception {
        String sql = """
                UPDATE dbo.class_sessions
                SET teacher_checkin_at = SYSUTCDATETIME(),
                    teacher_checkin_by = ?
                WHERE session_id = ?
                  AND status = N'SCHEDULED'
                  AND session_date = CAST(GETDATE() AS date)
                  AND teacher_checkin_at IS NULL
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            int updated = ps.executeUpdate();
            if (updated > 0) return null;
        }
        // More detailed message
        String checkSql = "SELECT status, session_date, teacher_checkin_at FROM dbo.class_sessions WHERE session_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "Buổi học không tồn tại.";
                String st = rs.getString("status");
                Date d = rs.getDate("session_date");
                if (d == null || !LocalDate.now().equals(d.toLocalDate())) return "Chỉ được check-in trong ngày.";
                if (!"SCHEDULED".equalsIgnoreCase(st)) return "Không thể check-in buổi đã hủy/hoàn thành.";
                Timestamp checkedAt = rs.getTimestamp("teacher_checkin_at");
                if (checkedAt != null) return "Đã check-in trước đó.";
            }
        }
        return "Không thể check-in.";
    }

    public List<ClassSession> listByTeacher(int teacherId) throws Exception {
        String sql = """
                SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, s.teacher_id, s.status,
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name, t.full_name AS teacher_name
                FROM dbo.class_sessions s
                JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                JOIN dbo.rooms r ON s.room_id = r.room_id
                JOIN dbo.teachers t ON s.teacher_id = t.teacher_id
                WHERE s.teacher_id = ?
                ORDER BY s.session_date, ts.start_time
                """;
        List<ClassSession> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<ClassSession> listByTeacherInRange(int teacherId, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = """
                WITH base AS (
                    SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, s.teacher_id, s.status,
                           ROW_NUMBER() OVER (PARTITION BY s.class_id ORDER BY s.session_date, ts.start_time, s.session_id) AS session_no,
                           c.class_code, c.class_name, cr.course_id, cr.course_name,
                           ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                           CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                           ts.start_time AS slot_start_time,
                           r.room_name, r.room_code,
                           t.full_name AS teacher_name,
                           a.assess_id, a.type AS assess_type, a.name AS assess_name
                    FROM dbo.class_sessions s
                    JOIN dbo.classes c ON s.class_id = c.class_id
                    JOIN dbo.courses cr ON c.course_id = cr.course_id
                    JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                    JOIN dbo.rooms r ON s.room_id = r.room_id
                    LEFT JOIN dbo.teachers t ON s.teacher_id = t.teacher_id
                    LEFT JOIN dbo.session_assessments sa ON sa.session_id = s.session_id
                    LEFT JOIN dbo.assessments a ON a.assess_id = sa.assess_id
                )
                SELECT session_id, class_id, session_date, slot_id, room_id, teacher_id, status,
                       session_no, class_code, class_name, course_id, course_name,
                       slot_name, start_time, end_time, room_name, room_code, teacher_name,
                       assess_id, assess_type, assess_name
                FROM base
                WHERE teacher_id = ?
                  AND session_date >= ?
                  AND session_date <= ?
                ORDER BY session_date, slot_start_time, session_id
                """;
        List<ClassSession> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapExtended(rs));
            }
        }
        return result;
    }

    public List<ClassSession> listByClassInRange(int classId, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = """
                WITH base AS (
                    SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, c.teacher_id, s.status,
                           ROW_NUMBER() OVER (PARTITION BY s.class_id ORDER BY s.session_date, ts.start_time, s.session_id) AS session_no,
                           c.class_code, c.class_name, cr.course_id, cr.course_name,
                           ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                           CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                           ts.start_time AS slot_start_time,
                           r.room_name, r.room_code,
                           t.full_name AS teacher_name,
                           a.assess_id, a.type AS assess_type, a.name AS assess_name
                    FROM dbo.class_sessions s
                    JOIN dbo.classes c ON s.class_id = c.class_id
                    JOIN dbo.courses cr ON c.course_id = cr.course_id
                    JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                    JOIN dbo.rooms r ON s.room_id = r.room_id
                    LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                    LEFT JOIN dbo.session_assessments sa ON sa.session_id = s.session_id
                    LEFT JOIN dbo.assessments a ON a.assess_id = sa.assess_id
                )
                SELECT session_id, class_id, session_date, slot_id, room_id, teacher_id, status,
                       session_no, class_code, class_name, course_id, course_name,
                       slot_name, start_time, end_time, room_name, room_code, teacher_name,
                       assess_id, assess_type, assess_name
                FROM base
                WHERE class_id = ?
                  AND session_date >= ?
                  AND session_date <= ?
                ORDER BY session_date, slot_start_time, session_id
                """;
        List<ClassSession> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapExtended(rs));
            }
        }
        return result;
    }

    public List<ClassSession> listByStudentInRange(int studentId, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = """
                WITH base AS (
                    SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, c.teacher_id, s.status,
                           ROW_NUMBER() OVER (PARTITION BY s.class_id ORDER BY s.session_date, ts.start_time, s.session_id) AS session_no,
                           c.class_code, c.class_name, cr.course_id, cr.course_name,
                           ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                           CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                           ts.start_time AS slot_start_time,
                           r.room_name, r.room_code,
                           t.full_name AS teacher_name,
                           a.assess_id, a.type AS assess_type, a.name AS assess_name
                    FROM dbo.class_sessions s
                    JOIN dbo.classes c ON s.class_id = c.class_id
                    JOIN dbo.courses cr ON c.course_id = cr.course_id
                    JOIN dbo.time_slots ts ON s.slot_id = ts.slot_id
                    JOIN dbo.rooms r ON s.room_id = r.room_id
                    LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                    LEFT JOIN dbo.session_assessments sa ON sa.session_id = s.session_id
                    LEFT JOIN dbo.assessments a ON a.assess_id = sa.assess_id
                )
                SELECT b.session_id, b.class_id, b.session_date, b.slot_id, b.room_id, b.teacher_id, b.status,
                       b.session_no, b.class_code, b.class_name, b.course_id, b.course_name,
                       b.slot_name, b.start_time, b.end_time, b.room_name, b.room_code, b.teacher_name,
                       b.assess_id, b.assess_type, b.assess_name,
                       att.status AS attendance_status,
                       FORMAT(att.marked_at, 'dd/MM/yyyy HH:mm:ss') AS attendance_marked_at
                FROM base b
                JOIN dbo.enrollments e ON e.class_id = b.class_id AND e.student_id = ? AND e.status IN (N'ACTIVE', N'COMPLETED')
                LEFT JOIN dbo.attendance att ON att.session_id = b.session_id AND att.enroll_id = e.enroll_id
                WHERE b.session_date >= ?
                  AND b.session_date <= ?
                ORDER BY b.session_date, b.slot_start_time, b.session_id
                """;
        List<ClassSession> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapExtended(rs));
            }
        }
        return result;
    }

    public int generateFromSchedules(int classId, LocalDate fromDate, LocalDate toDate) throws Exception {
        if (classId <= 0) throw new IllegalArgumentException("classId invalid");

        DateRange range = resolveClassDateRange(classId);
        LocalDate start = range.startDate;
        LocalDate end = range.endDate;

        if (fromDate != null && fromDate.isAfter(start)) start = fromDate;
        if (toDate != null && toDate.isBefore(end)) end = toDate;
        if (end.isBefore(start)) return 0;

        ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
        List<ClassSchedule> schedules = scheduleDAO.listByClass(classId);
        if (schedules.isEmpty()) return 0;

        String insertSql = """
                INSERT INTO dbo.class_sessions(class_id, session_date, slot_id, room_id, teacher_id, status)
                SELECT ?, ?, ?, ?, ?, N'SCHEDULED'
                WHERE NOT EXISTS (
                    SELECT 1 FROM dbo.class_sessions
                    WHERE class_id = ? AND session_date = ? AND slot_id = ?
                )
                """;

        int inserted = 0;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            for (ClassSchedule rule : schedules) {
                int dow = rule.getDayOfWeek(); // 1..7
                LocalDate d = firstDateOnOrAfter(start, dow);
                while (!d.isAfter(end)) {
                    ps.setInt(1, classId);
                    ps.setDate(2, Date.valueOf(d));
                    ps.setInt(3, rule.getSlotId());
                    ps.setInt(4, rule.getRoomId());
                    ps.setInt(5, rule.getTeacherId());
                    ps.setInt(6, classId);
                    ps.setDate(7, Date.valueOf(d));
                    ps.setInt(8, rule.getSlotId());
                    inserted += ps.executeUpdate();
                    d = d.plusDays(7);
                }
            }
        }
        return inserted;
    }

    public void updateTeacherForScheduledSessions(int classId, int teacherId) throws Exception {
        String sql = """
                UPDATE dbo.class_sessions
                SET teacher_id = ?
                WHERE class_id = ?
                  AND status = N'SCHEDULED'
                  AND session_date >= CAST(GETDATE() AS date)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            ps.executeUpdate();
        }
    }

    public int deleteScheduledFromDate(int classId, LocalDate fromDate) throws Exception {
        if (fromDate == null) fromDate = LocalDate.now();
        String sql = """
                DELETE FROM dbo.class_sessions
                WHERE class_id = ?
                  AND status = N'SCHEDULED'
                  AND session_date >= ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setDate(2, Date.valueOf(fromDate));
            return ps.executeUpdate();
        }
    }

    public int deleteScheduledByDaySlotFromDate(int classId, int dayOfWeek1to7, int slotId, LocalDate fromDate) throws Exception {
        if (fromDate == null) fromDate = LocalDate.now();
        if (dayOfWeek1to7 < 1 || dayOfWeek1to7 > 7) throw new IllegalArgumentException("dayOfWeek invalid");
        if (slotId <= 0) throw new IllegalArgumentException("slotId invalid");

        String delAssessSql = """
                DELETE sa
                FROM dbo.session_assessments sa
                JOIN dbo.class_sessions cs ON cs.session_id = sa.session_id
                WHERE cs.class_id = ?
                  AND cs.status = N'SCHEDULED'
                  AND cs.session_date >= ?
                  AND cs.slot_id = ?
                  AND DATEPART(WEEKDAY, cs.session_date) = ?
                """;
        String delSessionsSql = """
                DELETE FROM dbo.class_sessions
                WHERE class_id = ?
                  AND status = N'SCHEDULED'
                  AND session_date >= ?
                  AND slot_id = ?
                  AND DATEPART(WEEKDAY, session_date) = ?
                """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                // Ensure Monday=1 ... Sunday=7 (matches our schema convention).
                st.execute("SET DATEFIRST 1");
            }
            try {
                try (PreparedStatement ps = con.prepareStatement(delAssessSql)) {
                    ps.setInt(1, classId);
                    ps.setDate(2, Date.valueOf(fromDate));
                    ps.setInt(3, slotId);
                    ps.setInt(4, dayOfWeek1to7);
                    ps.executeUpdate();
                }

                int deleted;
                try (PreparedStatement ps = con.prepareStatement(delSessionsSql)) {
                    ps.setInt(1, classId);
                    ps.setDate(2, Date.valueOf(fromDate));
                    ps.setInt(3, slotId);
                    ps.setInt(4, dayOfWeek1to7);
                    deleted = ps.executeUpdate();
                }

                con.commit();
                return deleted;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private DateRange resolveClassDateRange(int classId) throws Exception {
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
                if (!rs.next()) throw new IllegalArgumentException("Class not found: " + classId);
                Date sd = rs.getDate("start_date");
                Date ed = rs.getDate("end_date");
                int weeks = rs.getInt("duration_weeks");
                LocalDate start = sd == null ? LocalDate.now() : sd.toLocalDate();
                LocalDate end;
                if (ed != null) end = ed.toLocalDate();
                else {
                    if (weeks <= 0) weeks = 1;
                    end = start.plusDays((long) weeks * 7L - 1L);
                }
                return new DateRange(start, end);
            }
        }
    }

    private static LocalDate firstDateOnOrAfter(LocalDate start, int dow1to7) {
        int startDow = start.getDayOfWeek().getValue(); // 1..7
        int delta = dow1to7 - startDow;
        if (delta < 0) delta += 7;
        return start.plusDays(delta);
    }

    private static ClassSession map(ResultSet rs) throws Exception {
        ClassSession s = new ClassSession();
        s.setSessionId(rs.getInt("session_id"));
        s.setClassId(rs.getInt("class_id"));
        Date d = rs.getDate("session_date");
        if (d != null) s.setSessionDate(d.toLocalDate());
        s.setSlotId(rs.getInt("slot_id"));
        s.setRoomId(rs.getInt("room_id"));
        s.setTeacherId(rs.getInt("teacher_id"));
        s.setStatus(rs.getString("status"));
        try { s.setSessionNo(rs.getInt("session_no")); } catch (Exception ignored) {}
        s.setSlotName(rs.getString("slot_name"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setRoomName(rs.getString("room_name"));
        s.setTeacherName(rs.getString("teacher_name"));
        return s;
    }

    private static boolean exists(Connection con, String sql, int classId, LocalDate date, int slotId, int sessionId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, slotId);
            ps.setInt(4, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean exists(Connection con, String sql, LocalDate date, int slotId, int xId, int sessionId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, slotId);
            ps.setInt(3, xId);
            ps.setInt(4, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static ClassSession mapExtended(ResultSet rs) throws Exception {
        ClassSession s = map(rs);
        try { s.setRoomCode(rs.getString("room_code")); } catch (Exception ignored) {}
        try { s.setClassCode(rs.getString("class_code")); } catch (Exception ignored) {}
        try { s.setClassName(rs.getString("class_name")); } catch (Exception ignored) {}
        try { s.setCourseId(rs.getInt("course_id")); } catch (Exception ignored) {}
        try { s.setCourseName(rs.getString("course_name")); } catch (Exception ignored) {}
        try { s.setAssessId((Integer) rs.getObject("assess_id")); } catch (Exception ignored) {}
        try { s.setAssessType(rs.getString("assess_type")); } catch (Exception ignored) {}
        try { s.setAssessName(rs.getString("assess_name")); } catch (Exception ignored) {}
        try { s.setAttendanceStatus(rs.getString("attendance_status")); } catch (Exception ignored) {}
        try { s.setAttendanceMarkedAt(rs.getString("attendance_marked_at")); } catch (Exception ignored) {}
        try { s.setTeacherCheckinAt(rs.getString("teacher_checkin_at")); } catch (Exception ignored) {}
        try { s.setSessionNo(rs.getInt("session_no")); } catch (Exception ignored) {}
        return s;
    }

    private static final class DateRange {
        final LocalDate startDate;
        final LocalDate endDate;

        DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
