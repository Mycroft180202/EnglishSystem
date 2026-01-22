package DAO;

import Model.ClassSchedule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassScheduleDAO extends DBContext {
    public static final class SlotOccupancy {
        private int dayOfWeek;
        private int slotId;
        private int classId;
        private String classCode;
        private String className;

        public int getDayOfWeek() { return dayOfWeek; }
        public int getSlotId() { return slotId; }
        public int getClassId() { return classId; }
        public String getClassCode() { return classCode; }
        public String getClassName() { return className; }
    }

    public List<ClassSchedule> listByClass(int classId) throws Exception {
        String sql = """
                SELECT cs.schedule_id, cs.class_id, cs.day_of_week, cs.slot_id, cs.room_id,
                       c.teacher_id,
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name, t.full_name AS teacher_name
                FROM dbo.class_schedules cs
                JOIN dbo.classes c ON cs.class_id = c.class_id
                JOIN dbo.time_slots ts ON cs.slot_id = ts.slot_id
                JOIN dbo.rooms r ON cs.room_id = r.room_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                WHERE cs.class_id = ?
                ORDER BY cs.day_of_week, ts.start_time
                """;
        List<ClassSchedule> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<SlotOccupancy> listRoomOccupancyForRange(int roomId, LocalDate start, LocalDate end, int excludeClassId) throws Exception {
        String sql = """
                SELECT cs.day_of_week, cs.slot_id, c.class_id, c.class_code, c.class_name
                FROM dbo.class_schedules cs
                JOIN dbo.classes c ON cs.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE cs.room_id = ?
                  AND c.class_id <> ?
                  AND c.start_date IS NOT NULL
                  AND c.start_date <= ?
                  AND COALESCE(c.end_date, DATEADD(day, (cr.duration_weeks * 7) - 1, c.start_date)) >= ?
                ORDER BY cs.day_of_week, cs.slot_id, c.class_id
                """;
        List<SlotOccupancy> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, Math.max(0, excludeClassId));
            ps.setDate(3, Date.valueOf(end));
            ps.setDate(4, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SlotOccupancy o = new SlotOccupancy();
                    o.dayOfWeek = rs.getInt("day_of_week");
                    o.slotId = rs.getInt("slot_id");
                    o.classId = rs.getInt("class_id");
                    o.classCode = rs.getString("class_code");
                    o.className = rs.getString("class_name");
                    result.add(o);
                }
            }
        }
        return result;
    }

    public SlotOccupancy findRoomConflict(int classId, int roomId, int dayOfWeek, int slotId, int excludeScheduleId) throws Exception {
        ClassDAO.DateRange range = new ClassDAO().resolveDateRange(classId);
        if (range == null) return null;
        return findRoomConflictForRange(classId, roomId, dayOfWeek, slotId, excludeScheduleId, range.startDate, range.endDate);
    }

    private SlotOccupancy findRoomConflictForRange(int classId, int roomId, int dayOfWeek, int slotId, int excludeScheduleId,
                                                   LocalDate start, LocalDate end) throws Exception {
        String sql = """
                SELECT TOP 1 cs.day_of_week, cs.slot_id, c.class_id, c.class_code, c.class_name
                FROM dbo.class_schedules cs
                JOIN dbo.classes c ON cs.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE cs.room_id = ?
                  AND cs.day_of_week = ?
                  AND cs.slot_id = ?
                  AND cs.class_id <> ?
                  AND cs.schedule_id <> ?
                  AND c.start_date IS NOT NULL
                  AND c.start_date <= ?
                  AND COALESCE(c.end_date, DATEADD(day, (cr.duration_weeks * 7) - 1, c.start_date)) >= ?
                ORDER BY c.start_date DESC, c.class_id DESC
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, dayOfWeek);
            ps.setInt(3, slotId);
            ps.setInt(4, classId);
            ps.setInt(5, Math.max(0, excludeScheduleId));
            ps.setDate(6, Date.valueOf(end));
            ps.setDate(7, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SlotOccupancy o = new SlotOccupancy();
                o.dayOfWeek = rs.getInt("day_of_week");
                o.slotId = rs.getInt("slot_id");
                o.classId = rs.getInt("class_id");
                o.classCode = rs.getString("class_code");
                o.className = rs.getString("class_name");
                return o;
            }
        }
    }

    public SlotOccupancy findTeacherConflict(int classId, int teacherId, int dayOfWeek, int slotId, int excludeScheduleId) throws Exception {
        ClassDAO.DateRange range = new ClassDAO().resolveDateRange(classId);
        if (range == null) return null;
        String sql = """
                SELECT TOP 1 cs.day_of_week, cs.slot_id, c.class_id, c.class_code, c.class_name
                FROM dbo.class_schedules cs
                JOIN dbo.classes c ON cs.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE cs.teacher_id = ?
                  AND cs.day_of_week = ?
                  AND cs.slot_id = ?
                  AND cs.class_id <> ?
                  AND cs.schedule_id <> ?
                  AND c.start_date IS NOT NULL
                  AND c.start_date <= ?
                  AND COALESCE(c.end_date, DATEADD(day, (cr.duration_weeks * 7) - 1, c.start_date)) >= ?
                ORDER BY c.start_date DESC, c.class_id DESC
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, dayOfWeek);
            ps.setInt(3, slotId);
            ps.setInt(4, classId);
            ps.setInt(5, Math.max(0, excludeScheduleId));
            ps.setDate(6, Date.valueOf(range.endDate));
            ps.setDate(7, Date.valueOf(range.startDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SlotOccupancy o = new SlotOccupancy();
                o.dayOfWeek = rs.getInt("day_of_week");
                o.slotId = rs.getInt("slot_id");
                o.classId = rs.getInt("class_id");
                o.classCode = rs.getString("class_code");
                o.className = rs.getString("class_name");
                return o;
            }
        }
    }

    public int create(ClassSchedule s) throws Exception {
        SlotOccupancy roomConflict = findRoomConflict(s.getClassId(), s.getRoomId(), s.getDayOfWeek(), s.getSlotId(), 0);
        if (roomConflict != null) {
            throw new IllegalStateException("ROOM_CONFLICT:" + roomConflict.getClassId());
        }
        SlotOccupancy teacherConflict = findTeacherConflict(s.getClassId(), s.getTeacherId(), s.getDayOfWeek(), s.getSlotId(), 0);
        if (teacherConflict != null) {
            throw new IllegalStateException("TEACHER_CONFLICT:" + teacherConflict.getClassId());
        }

        String sql = """
                INSERT INTO dbo.class_schedules(class_id, day_of_week, slot_id, room_id, teacher_id)
                VALUES(?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getClassId());
            ps.setInt(2, s.getDayOfWeek());
            ps.setInt(3, s.getSlotId());
            ps.setInt(4, s.getRoomId());
            ps.setInt(5, s.getTeacherId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(ClassSchedule s) throws Exception {
        SlotOccupancy roomConflict = findRoomConflict(s.getClassId(), s.getRoomId(), s.getDayOfWeek(), s.getSlotId(), s.getScheduleId());
        if (roomConflict != null) {
            throw new IllegalStateException("ROOM_CONFLICT:" + roomConflict.getClassId());
        }
        SlotOccupancy teacherConflict = findTeacherConflict(s.getClassId(), s.getTeacherId(), s.getDayOfWeek(), s.getSlotId(), s.getScheduleId());
        if (teacherConflict != null) {
            throw new IllegalStateException("TEACHER_CONFLICT:" + teacherConflict.getClassId());
        }

        String sql = """
                UPDATE dbo.class_schedules
                SET day_of_week = ?, slot_id = ?, room_id = ?, teacher_id = ?
                WHERE schedule_id = ? AND class_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.getDayOfWeek());
            ps.setInt(2, s.getSlotId());
            ps.setInt(3, s.getRoomId());
            ps.setInt(4, s.getTeacherId());
            ps.setInt(5, s.getScheduleId());
            ps.setInt(6, s.getClassId());
            ps.executeUpdate();
        }
    }

    public void updateTeacherForClass(int classId, int teacherId) throws Exception {
        String sql = "UPDATE dbo.class_schedules SET teacher_id = ? WHERE class_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            ps.executeUpdate();
        }
    }

    public void delete(int scheduleId) throws Exception {
        String sql = "DELETE FROM dbo.class_schedules WHERE schedule_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.executeUpdate();
        }
    }

    public ClassSchedule findById(int scheduleId) throws Exception {
        String sql = """
                SELECT cs.schedule_id, cs.class_id, cs.day_of_week, cs.slot_id, cs.room_id, cs.teacher_id,
                       ts.name AS slot_name, CONVERT(varchar(5), ts.start_time, 108) AS start_time,
                       CONVERT(varchar(5), ts.end_time, 108) AS end_time,
                       r.room_name
                FROM dbo.class_schedules cs
                JOIN dbo.time_slots ts ON cs.slot_id = ts.slot_id
                JOIN dbo.rooms r ON cs.room_id = r.room_id
                WHERE cs.schedule_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ClassSchedule s = new ClassSchedule();
                s.setScheduleId(rs.getInt("schedule_id"));
                s.setClassId(rs.getInt("class_id"));
                s.setDayOfWeek(rs.getInt("day_of_week"));
                s.setSlotId(rs.getInt("slot_id"));
                s.setRoomId(rs.getInt("room_id"));
                s.setTeacherId(rs.getInt("teacher_id"));
                s.setSlotName(rs.getString("slot_name"));
                s.setStartTime(rs.getString("start_time"));
                s.setEndTime(rs.getString("end_time"));
                s.setRoomName(rs.getString("room_name"));
                return s;
            }
        }
    }

    private static ClassSchedule map(ResultSet rs) throws Exception {
        ClassSchedule s = new ClassSchedule();
        s.setScheduleId(rs.getInt("schedule_id"));
        s.setClassId(rs.getInt("class_id"));
        s.setDayOfWeek(rs.getInt("day_of_week"));
        s.setSlotId(rs.getInt("slot_id"));
        s.setRoomId(rs.getInt("room_id"));
        s.setTeacherId(rs.getInt("teacher_id"));
        s.setSlotName(rs.getString("slot_name"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setRoomName(rs.getString("room_name"));
        s.setTeacherName(rs.getString("teacher_name"));
        return s;
    }
}
