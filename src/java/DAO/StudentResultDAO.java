package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentResultDAO extends DBContext {
    public static final class ResultRow {
        private int enrollId;
        private String className;
        private String courseName;
        private String assessmentName;
        private String assessmentType;
        private BigDecimal scoreValue;
        private BigDecimal maxScore;

        public int getEnrollId() {
            return enrollId;
        }

        public void setEnrollId(int enrollId) {
            this.enrollId = enrollId;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getAssessmentName() {
            return assessmentName;
        }

        public void setAssessmentName(String assessmentName) {
            this.assessmentName = assessmentName;
        }

        public String getAssessmentType() {
            return assessmentType;
        }

        public void setAssessmentType(String assessmentType) {
            this.assessmentType = assessmentType;
        }

        public BigDecimal getScoreValue() {
            return scoreValue;
        }

        public void setScoreValue(BigDecimal scoreValue) {
            this.scoreValue = scoreValue;
        }

        public BigDecimal getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(BigDecimal maxScore) {
            this.maxScore = maxScore;
        }
    }

    public List<ResultRow> listResultsByStudent(int studentId) throws Exception {
        String sql = """
                SELECT e.enroll_id, c.class_name, cr.course_name,
                       a.name AS assessment_name, a.type AS assessment_type, a.max_score,
                       sc.score_value
                FROM dbo.enrollments e
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                LEFT JOIN dbo.assessments a ON a.course_id = cr.course_id
                LEFT JOIN dbo.scores sc ON sc.enroll_id = e.enroll_id AND sc.assess_id = a.assess_id
                WHERE e.student_id = ? AND e.status IN (N'ACTIVE', N'COMPLETED')
                ORDER BY e.enroll_id ASC, a.assess_id ASC
                """;
        List<ResultRow> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultRow r = new ResultRow();
                    r.setEnrollId(rs.getInt("enroll_id"));
                    r.setClassName(rs.getString("class_name"));
                    r.setCourseName(rs.getString("course_name"));
                    r.setAssessmentName(rs.getString("assessment_name"));
                    r.setAssessmentType(rs.getString("assessment_type"));
                    r.setMaxScore(rs.getBigDecimal("max_score"));
                    r.setScoreValue(rs.getBigDecimal("score_value"));
                    result.add(r);
                }
            }
        }
        return result;
    }
}
