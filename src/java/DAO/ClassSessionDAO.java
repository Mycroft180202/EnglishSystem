package DAO;

import Model.ClassSchedule;
import Model.ClassSession;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class ClassSessionDAO extends DBContext {
    public ClassSession findByIdExtended(int sessionId) throws Exception {
        String sql = """
                SELECT s.session_id, s.class_id, s.session_date, s.slot_id, s.room_id, s.teacher_id, s.status,
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
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name, t.full_name AS teacher_name
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
        s.setSlotName(rs.getString("slot_name"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setRoomName(rs.getString("room_name"));
        s.setTeacherName(rs.getString("teacher_name"));
        return s;
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
