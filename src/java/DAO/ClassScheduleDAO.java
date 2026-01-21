package DAO;

import Model.ClassSchedule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassScheduleDAO extends DBContext {
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

    public int create(ClassSchedule s) throws Exception {
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
