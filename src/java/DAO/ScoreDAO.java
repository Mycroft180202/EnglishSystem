package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class ScoreDAO extends DBContext {
    public Map<Integer, BigDecimal> mapScoresByEnrollId(int assessId, int classId) throws Exception {
        String sql = """
                SELECT s.enroll_id, sc.score_value
                FROM dbo.scores sc
                JOIN dbo.enrollments s ON sc.enroll_id = s.enroll_id
                WHERE sc.assess_id = ? AND s.class_id = ?
                """;
        Map<Integer, BigDecimal> result = new HashMap<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assessId);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("enroll_id"), rs.getBigDecimal("score_value"));
                }
            }
        }
        return result;
    }

    public void upsertScore(int assessId, int enrollId, BigDecimal value, Integer gradedByUserId) throws Exception {
        String mergeSql = """
                MERGE dbo.scores AS target
                USING (SELECT ? AS assess_id, ? AS enroll_id) AS source
                ON (target.assess_id = source.assess_id AND target.enroll_id = source.enroll_id)
                WHEN MATCHED THEN
                    UPDATE SET score_value = ?, graded_at = SYSUTCDATETIME(), graded_by = ?
                WHEN NOT MATCHED THEN
                    INSERT (assess_id, enroll_id, score_value, graded_at, graded_by)
                    VALUES (?, ?, ?, SYSUTCDATETIME(), ?);
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(mergeSql)) {
            ps.setInt(1, assessId);
            ps.setInt(2, enrollId);
            if (value == null) ps.setNull(3, Types.DECIMAL);
            else ps.setBigDecimal(3, value);
            if (gradedByUserId == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, gradedByUserId);

            ps.setInt(5, assessId);
            ps.setInt(6, enrollId);
            if (value == null) ps.setNull(7, Types.DECIMAL);
            else ps.setBigDecimal(7, value);
            if (gradedByUserId == null) ps.setNull(8, Types.INTEGER);
            else ps.setInt(8, gradedByUserId);

            ps.executeUpdate();
        }
    }
}

