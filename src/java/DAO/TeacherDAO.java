package DAO;

import Model.Teacher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO extends DBContext {
    public List<Teacher> listAll(String statusFilter) throws Exception {
        String sql = """
                SELECT teacher_id, full_name, email, phone, level, status
                FROM dbo.teachers
                WHERE (? IS NULL OR status = ?)
                ORDER BY teacher_id ASC
                """;
        List<Teacher> result = new ArrayList<>();
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

    public List<Teacher> listActive() throws Exception {
        return listAll("ACTIVE");
    }

    public Teacher findById(int teacherId) throws Exception {
        String sql = """
                SELECT teacher_id, full_name, email, phone, level, status
                FROM dbo.teachers
                WHERE teacher_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(Teacher t) throws Exception {
        String sql = """
                INSERT INTO dbo.teachers(full_name, email, phone, level, status)
                VALUES(?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, t, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(Teacher t) throws Exception {
        String sql = """
                UPDATE dbo.teachers
                SET full_name = ?, email = ?, phone = ?, level = ?, status = ?
                WHERE teacher_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, t, true);
            ps.executeUpdate();
        }
    }

    public void setStatus(int teacherId, String status) throws Exception {
        String sql = "UPDATE dbo.teachers SET status = ? WHERE teacher_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, teacherId);
            ps.executeUpdate();
        }
    }

    private static Teacher map(ResultSet rs) throws Exception {
        Teacher t = new Teacher();
        t.setTeacherId(rs.getInt("teacher_id"));
        t.setFullName(rs.getString("full_name"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setLevel(rs.getString("level"));
        t.setStatus(rs.getString("status"));
        return t;
    }

    private static void bind(PreparedStatement ps, Teacher t, boolean includeIdAtEnd) throws Exception {
        ps.setString(1, t.getFullName());
        ps.setString(2, t.getEmail());
        ps.setString(3, t.getPhone());
        ps.setString(4, t.getLevel());
        ps.setString(5, t.getStatus());
        if (includeIdAtEnd) ps.setInt(6, t.getTeacherId());
    }
}
