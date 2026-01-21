package DAO;

import Model.Invoice;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StudentFeeDAO extends DBContext {
    public List<Invoice> listInvoicesByStudent(int studentId) throws Exception {
        String sql = """
                SELECT i.invoice_id, i.enroll_id, i.invoice_code, i.total_amount, i.discount_amount, i.paid_amount,
                       i.status, i.issued_at, i.issued_by,
                       s.full_name AS student_name, s.phone AS student_phone,
                       c.class_name, cr.course_name
                FROM dbo.invoices i
                JOIN dbo.enrollments e ON i.enroll_id = e.enroll_id
                JOIN dbo.students s ON e.student_id = s.student_id
                JOIN dbo.classes c ON e.class_id = c.class_id
                JOIN dbo.courses cr ON c.course_id = cr.course_id
                WHERE e.student_id = ?
                ORDER BY i.invoice_id ASC
                """;
        List<Invoice> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice i = new Invoice();
                    i.setInvoiceId(rs.getInt("invoice_id"));
                    i.setEnrollId(rs.getInt("enroll_id"));
                    i.setInvoiceCode(rs.getString("invoice_code"));
                    i.setTotalAmount(rs.getBigDecimal("total_amount"));
                    i.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    i.setPaidAmount(rs.getBigDecimal("paid_amount"));
                    i.setStatus(rs.getString("status"));
                    Timestamp issuedAt = rs.getTimestamp("issued_at");
                    if (issuedAt != null) i.setIssuedAt(issuedAt.toInstant());
                    int issuedBy = rs.getInt("issued_by");
                    i.setIssuedBy(rs.wasNull() ? null : issuedBy);
                    i.setStudentName(rs.getString("student_name"));
                    i.setStudentPhone(rs.getString("student_phone"));
                    i.setClassName(rs.getString("class_name"));
                    i.setCourseName(rs.getString("course_name"));
                    result.add(i);
                }
            }
        }
        return result;
    }
}
