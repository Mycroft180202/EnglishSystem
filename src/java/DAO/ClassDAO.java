package DAO;

import Model.CenterClass;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO extends DBContext {
    public List<CenterClass> listAll(String statusFilter) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name
                FROM dbo.classes c
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.teachers t ON c.teacher_id = t.teacher_id
                LEFT JOIN dbo.rooms r ON c.room_id = r.room_id
                WHERE (? IS NULL OR c.status = ?)
                ORDER BY c.class_id ASC
                """;
        List<CenterClass> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public CenterClass findById(int classId) throws Exception {
        String sql = """
                SELECT c.class_id, c.class_code, c.class_name, c.capacity, c.start_date, c.end_date, c.status,
                       c.course_id, cr.course_name,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name
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
                       c.course_id, cr.course_name,
                       c.teacher_id, t.full_name AS teacher_name,
                       c.room_id, r.room_name
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

    private static CenterClass map(ResultSet rs) throws Exception {
        CenterClass c = new CenterClass();
        c.setClassId(rs.getInt("class_id"));
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseName(rs.getString("course_name"));
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
}
