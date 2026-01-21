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

    private static final class DateRange {
        final LocalDate startDate;
        final LocalDate endDate;

        DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
