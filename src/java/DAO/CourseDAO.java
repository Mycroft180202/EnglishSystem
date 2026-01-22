package DAO;

import Model.Course;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO extends DBContext {
    public List<Course> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT course_id, course_code, course_name, description, level, duration_weeks, standard_fee, status
                FROM dbo.courses
                WHERE (? IS NULL OR status = ?)
                  AND (? IS NULL OR course_code LIKE ? OR course_name LIKE ?)
                ORDER BY course_id ASC
                """;
        List<Course> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = q == null ? null : "%" + q + "%";
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Course findById(int courseId) throws Exception {
        String sql = """
                SELECT course_id, course_code, course_name, description, level, duration_weeks, standard_fee, status
                FROM dbo.courses
                WHERE course_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Course findByCode(String courseCode) throws Exception {
        String sql = """
                SELECT course_id, course_code, course_name, description, level, duration_weeks, standard_fee, status
                FROM dbo.courses
                WHERE course_code = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(Course course) throws Exception {
        String sql = """
                INSERT INTO dbo.courses(course_code, course_name, description, level, duration_weeks, standard_fee, status)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, course, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(Course course) throws Exception {
        String sql = """
                UPDATE dbo.courses
                SET course_code = ?, course_name = ?, description = ?, level = ?, duration_weeks = ?, standard_fee = ?, status = ?
                WHERE course_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, course, true);
            ps.executeUpdate();
        }
    }

    public void setStatus(int courseId, String status) throws Exception {
        String sql = "UPDATE dbo.courses SET status = ? WHERE course_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    private static Course map(ResultSet rs) throws Exception {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setDescription(rs.getString("description"));
        c.setLevel(rs.getString("level"));
        c.setDurationWeeks(rs.getInt("duration_weeks"));
        c.setStandardFee(rs.getBigDecimal("standard_fee"));
        c.setStatus(rs.getString("status"));
        return c;
    }

    private static void bind(PreparedStatement ps, Course c, boolean includeIdAtEnd) throws Exception {
        ps.setString(1, c.getCourseCode());
        ps.setString(2, c.getCourseName());
        ps.setString(3, c.getDescription());
        ps.setString(4, c.getLevel());
        ps.setInt(5, c.getDurationWeeks());
        BigDecimal fee = c.getStandardFee() == null ? BigDecimal.ZERO : c.getStandardFee();
        ps.setBigDecimal(6, fee);
        ps.setString(7, c.getStatus());
        if (includeIdAtEnd) ps.setInt(8, c.getCourseId());
    }

    public DeleteResult deleteCascade(int courseId) throws Exception {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                int classCount = count(con, "SELECT COUNT(*) FROM dbo.classes WHERE course_id = ?", courseId);
                if (classCount > 0) {
                    con.rollback();
                    return DeleteResult.fail("Không thể xóa khóa học: còn " + classCount + " lớp đang dùng khóa học này.");
                }

                // Extra safety: delete anything still attached to this course.
                exec(con, """
                        DELETE sa
                        FROM dbo.session_assessments sa
                        JOIN dbo.assessments a ON sa.assess_id = a.assess_id
                        WHERE a.course_id = ?
                        """, courseId);
                exec(con, """
                        DELETE s
                        FROM dbo.scores s
                        JOIN dbo.assessments a ON s.assess_id = a.assess_id
                        WHERE a.course_id = ?
                        """, courseId);
                exec(con, "DELETE FROM dbo.assessments WHERE course_id = ?", courseId);

                int deleted = exec(con, "DELETE FROM dbo.courses WHERE course_id = ?", courseId);
                con.commit();
                if (deleted <= 0) return DeleteResult.fail("Không tìm thấy khóa học để xóa.");
                return DeleteResult.ok("Đã xóa khóa học.");
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
