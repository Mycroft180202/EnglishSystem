package DAO;

import Model.Assessment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDAO extends DBContext {
    public List<Assessment> listByCourse(int courseId) throws Exception {
        String sql = """
                SELECT assess_id, course_id, name, weight, max_score, type
                FROM dbo.assessments
                WHERE course_id = ?
                ORDER BY assess_id ASC
                """;
        List<Assessment> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Assessment findById(int assessId) throws Exception {
        String sql = "SELECT assess_id, course_id, name, weight, max_score, type FROM dbo.assessments WHERE assess_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(Assessment a) throws Exception {
        String sql = "INSERT INTO dbo.assessments(course_id, name, weight, max_score, type) VALUES(?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, a, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(Assessment a) throws Exception {
        String sql = "UPDATE dbo.assessments SET course_id=?, name=?, weight=?, max_score=?, type=? WHERE assess_id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, a, true);
            ps.executeUpdate();
        }
    }

    public void delete(int assessId) throws Exception {
        String sql = "DELETE FROM dbo.assessments WHERE assess_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assessId);
            ps.executeUpdate();
        }
    }

    private static Assessment map(ResultSet rs) throws Exception {
        Assessment a = new Assessment();
        a.setAssessId(rs.getInt("assess_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setName(rs.getString("name"));
        a.setWeight(rs.getBigDecimal("weight"));
        a.setMaxScore(rs.getBigDecimal("max_score"));
        a.setType(rs.getString("type"));
        return a;
    }

    private static void bind(PreparedStatement ps, Assessment a, boolean includeIdAtEnd) throws Exception {
        ps.setInt(1, a.getCourseId());
        ps.setString(2, a.getName());
        BigDecimal w = a.getWeight() == null ? BigDecimal.ZERO : a.getWeight();
        BigDecimal max = a.getMaxScore() == null ? BigDecimal.TEN : a.getMaxScore();
        ps.setBigDecimal(3, w);
        ps.setBigDecimal(4, max);
        ps.setString(5, a.getType());
        if (includeIdAtEnd) ps.setInt(6, a.getAssessId());
    }
}
