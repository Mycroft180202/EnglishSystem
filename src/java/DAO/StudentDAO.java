package DAO;

import Model.Student;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO extends DBContext {
    public List<Student> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT student_id, full_name, dob, gender, email, phone, address, input_level, status
                FROM dbo.students
                WHERE (? IS NULL OR status = ?)
                  AND (? IS NULL OR full_name LIKE ? OR phone LIKE ? OR email LIKE ?)
                ORDER BY student_id ASC
                """;
        List<Student> result = new ArrayList<>();
        String like = q == null ? null : "%" + q + "%";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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

    public List<Student> listActive() throws Exception {
        return listAll("ACTIVE", null);
    }

    public Student findById(int studentId) throws Exception {
        String sql = """
                SELECT student_id, full_name, dob, gender, email, phone, address, input_level, status
                FROM dbo.students
                WHERE student_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(Student s) throws Exception {
        String sql = """
                INSERT INTO dbo.students(full_name, dob, gender, email, phone, address, input_level, status)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, s, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(Student s) throws Exception {
        String sql = """
                UPDATE dbo.students
                SET full_name = ?, dob = ?, gender = ?, email = ?, phone = ?, address = ?, input_level = ?, status = ?
                WHERE student_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, s, true);
            ps.executeUpdate();
        }
    }

    public void setStatus(int studentId, String status) throws Exception {
        String sql = "UPDATE dbo.students SET status = ? WHERE student_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private static Student map(ResultSet rs) throws Exception {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setFullName(rs.getString("full_name"));
        Date dob = rs.getDate("dob");
        if (dob != null) s.setDob(dob.toLocalDate());
        s.setGender(rs.getString("gender"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setInputLevel(rs.getString("input_level"));
        s.setStatus(rs.getString("status"));
        return s;
    }

    private static void bind(PreparedStatement ps, Student s, boolean includeIdAtEnd) throws Exception {
        ps.setString(1, s.getFullName());
        ps.setDate(2, s.getDob() == null ? null : Date.valueOf(s.getDob()));
        ps.setString(3, s.getGender());
        ps.setString(4, s.getEmail());
        ps.setString(5, s.getPhone());
        ps.setString(6, s.getAddress());
        ps.setString(7, s.getInputLevel());
        ps.setString(8, s.getStatus());
        if (includeIdAtEnd) ps.setInt(9, s.getStudentId());
    }
}
