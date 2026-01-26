package DAO;

import Model.Teacher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO extends DBContext {
    public List<Teacher> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT teacher_id, full_name, email, phone, level, status
                FROM dbo.teachers
                WHERE (? IS NULL OR status = ?)
                  AND (? IS NULL OR full_name LIKE ? OR email LIKE ? OR phone LIKE ?)
                ORDER BY teacher_id ASC
                """;
        List<Teacher> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = q == null ? null : "%" + q + "%";
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<Teacher> listActive() throws Exception {
        return listAll("ACTIVE", null);
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

    public void updateContact(int teacherId, String fullName, String email, String phone) throws Exception {
        String sql = """
                UPDATE dbo.teachers
                SET full_name = NULLIF(?, N''),
                    email = NULLIF(?, N''),
                    phone = NULLIF(?, N'')
                WHERE teacher_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName == null ? "" : fullName.trim());
            ps.setString(2, email == null ? "" : email.trim());
            ps.setString(3, phone == null ? "" : phone.trim());
            ps.setInt(4, teacherId);
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

    public DeleteResult deleteSafe(int teacherId) throws Exception {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                int scheduleCount = count(con, "SELECT COUNT(*) FROM dbo.class_schedules WHERE teacher_id = ?", teacherId);
                int sessionCount = count(con, "SELECT COUNT(*) FROM dbo.class_sessions WHERE teacher_id = ?", teacherId);
                if (scheduleCount > 0 || sessionCount > 0) {
                    con.rollback();
                    return DeleteResult.fail("Không thể xóa giáo viên: đang được dùng trong lịch/buổi học (schedules=" + scheduleCount + ", sessions=" + sessionCount + ").");
                }

                exec(con, "UPDATE dbo.classes SET teacher_id = NULL WHERE teacher_id = ?", teacherId);
                exec(con, "UPDATE dbo.users SET teacher_id = NULL WHERE teacher_id = ?", teacherId);
                exec(con, "DELETE FROM dbo.teacher_certificates WHERE teacher_id = ?", teacherId);
                int deleted = exec(con, "DELETE FROM dbo.teachers WHERE teacher_id = ?", teacherId);
                con.commit();
                if (deleted <= 0) return DeleteResult.fail("Không tìm thấy giáo viên để xóa.");
                return DeleteResult.ok("Đã xóa giáo viên.");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static int count(Connection con, String sql, int id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
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
