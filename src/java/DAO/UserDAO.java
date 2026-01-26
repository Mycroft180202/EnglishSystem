package DAO;

import Model.User;
import Util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserDAO extends DBContext {
    public boolean anyUserExists() throws Exception {
        String sql = "SELECT TOP 1 1 FROM dbo.users";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    public User findByUsername(String username) throws Exception {
        String sql = """
                SELECT u.user_id, u.username, u.password_hash,
                       u.full_name, u.email, u.phone, u.address,
                       u.email_verified, u.email_verify_token, u.email_verify_expires,
                       u.must_change_password, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
                FROM dbo.users u
                LEFT JOIN dbo.user_roles ur ON u.user_id = ur.user_id
                LEFT JOIN dbo.roles r ON ur.role_id = r.role_id
                WHERE u.username = ?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUserWithRoles(rs);
            }
        }
    }

    public User findById(int userId) throws Exception {
        String sql = """
                SELECT u.user_id, u.username, u.password_hash,
                       u.full_name, u.email, u.phone, u.address,
                       u.email_verified, u.email_verify_token, u.email_verify_expires,
                       u.must_change_password, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
                FROM dbo.users u
                LEFT JOIN dbo.user_roles ur ON u.user_id = ur.user_id
                LEFT JOIN dbo.roles r ON ur.role_id = r.role_id
                WHERE u.user_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUserWithRoles(rs);
            }
        }
    }

    public int createUser(String username, String passwordHash) throws Exception {
        String sql = "INSERT INTO dbo.users(username, password_hash, status) VALUES(?, ?, N'ACTIVE')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public int createUserWithRole(String username, String passwordHash, String roleCode) throws Exception {
        return createUserWithRole(username, passwordHash, roleCode, false);
    }

    public int createUserWithRole(String username, String passwordHash, String roleCode, boolean mustChangePassword) throws Exception {
        String insertUserSql = "INSERT INTO dbo.users(username, password_hash, must_change_password, status) VALUES(?, ?, ?, N'ACTIVE')";
        String insertRoleSql = """
                INSERT INTO dbo.user_roles(user_id, role_id)
                SELECT ?, role_id FROM dbo.roles WHERE role_code = ?
                """;

        try (Connection con = getConnection()) {
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, passwordHash);
                    ps.setBoolean(3, mustChangePassword);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("No generated key returned");
                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertRoleSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, roleCode);
                    int affected = ps.executeUpdate();
                    if (affected != 1) throw new IllegalArgumentException("Role code not found: " + roleCode);
                }

                con.commit();
                return userId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public int createTeacherUser(int teacherId, String username, String passwordHash) throws Exception {
        String insertUserSql = "INSERT INTO dbo.users(username, password_hash, teacher_id, must_change_password, status) VALUES(?, ?, ?, 1, N'ACTIVE')";
        String insertRoleSql = """
                INSERT INTO dbo.user_roles(user_id, role_id)
                SELECT ?, role_id FROM dbo.roles WHERE role_code = N'TEACHER'
                """;

        try (Connection con = getConnection()) {
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, passwordHash);
                    ps.setInt(3, teacherId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("No generated key returned");
                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertRoleSql)) {
                    ps.setInt(1, userId);
                    int affected = ps.executeUpdate();
                    if (affected != 1) throw new IllegalStateException("Role TEACHER not found");
                }

                con.commit();
                return userId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public int createStudentUser(int studentId, String username, String passwordHash) throws Exception {
        return createStudentUser(studentId, username, passwordHash, true, null, null);
    }

    public int createStudentUser(int studentId, String username, String passwordHash, boolean mustChangePassword, String fullName, String email) throws Exception {
        String insertUserSql = """
                INSERT INTO dbo.users(username, password_hash, student_id, must_change_password, status, full_name, email)
                VALUES(?, ?, ?, ?, N'ACTIVE', NULLIF(?, N''), NULLIF(?, N''))
                """;
        String insertRoleSql = """
                INSERT INTO dbo.user_roles(user_id, role_id)
                SELECT ?, role_id FROM dbo.roles WHERE role_code = N'STUDENT'
                """;

        try (Connection con = getConnection()) {
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, passwordHash);
                    ps.setInt(3, studentId);
                    ps.setBoolean(4, mustChangePassword);
                    ps.setString(5, fullName == null ? "" : fullName.trim());
                    ps.setString(6, email == null ? "" : email.trim());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("No generated key returned");
                        userId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertRoleSql)) {
                    ps.setInt(1, userId);
                    int affected = ps.executeUpdate();
                    if (affected != 1) throw new IllegalStateException("Role STUDENT not found");
                }

                con.commit();
                return userId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public User findStudentAccount(int studentId) throws Exception {
        String sql = """
                SELECT u.user_id, u.username, u.password_hash, u.must_change_password, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
                FROM dbo.users u
                LEFT JOIN dbo.user_roles ur ON u.user_id = ur.user_id
                LEFT JOIN dbo.roles r ON ur.role_id = r.role_id
                WHERE u.student_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUserWithRoles(rs);
            }
        }
    }

    public User findTeacherAccount(int teacherId) throws Exception {
        String sql = """
                SELECT u.user_id, u.username, u.password_hash, u.must_change_password, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
                FROM dbo.users u
                LEFT JOIN dbo.user_roles ur ON u.user_id = ur.user_id
                LEFT JOIN dbo.roles r ON ur.role_id = r.role_id
                WHERE u.teacher_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUserWithRoles(rs);
            }
        }
    }

    public void assignRoleByCode(int userId, String roleCode) throws Exception {
        String sql = """
                INSERT INTO dbo.user_roles(user_id, role_id)
                SELECT ?, role_id FROM dbo.roles WHERE role_code = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleCode);
            int affected = ps.executeUpdate();
            if (affected != 1) throw new IllegalArgumentException("Role code not found: " + roleCode);
        }
    }

    public void updatePassword(int userId, String newPasswordHash) throws Exception {
        String sql = "UPDATE dbo.users SET password_hash = ?, must_change_password = 0 WHERE user_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void setStatusByTeacherId(int teacherId, String status) throws Exception {
        String st = normalizeUserStatusFromEntity(status);
        String sql = "UPDATE dbo.users SET status = ? WHERE teacher_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, st);
            ps.setInt(2, teacherId);
            ps.executeUpdate();
        }
    }

    public void setStatusByStudentId(int studentId, String status) throws Exception {
        String st = normalizeUserStatusFromEntity(status);
        String sql = "UPDATE dbo.users SET status = ? WHERE student_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, st);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    private static String normalizeUserStatusFromEntity(String status) {
        if (status == null) return "ACTIVE";
        String s = status.trim().toUpperCase();
        // Entity tables use ACTIVE/INACTIVE, but dbo.users allows ACTIVE/LOCKED/DISABLED.
        if ("INACTIVE".equals(s)) return "DISABLED";
        if ("ACTIVE".equals(s)) return "ACTIVE";
        if ("LOCKED".equals(s) || "DISABLED".equals(s)) return s;
        return "ACTIVE";
    }

    public User authenticate(String username, char[] password) throws Exception {
        User user = findByUsername(username);
        if (user == null) return null;
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) return null;
        return PasswordUtil.verifyPassword(password, user.getPasswordHash()) ? user : null;
    }

    private static User mapUserWithRoles(ResultSet rs) throws Exception {
        User user = null;
        Set<String> roles = new LinkedHashSet<>();
        while (rs.next()) {
            if (user == null) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                try { user.setFullName(rs.getString("full_name")); } catch (Exception ignored) {}
                try { user.setEmail(rs.getString("email")); } catch (Exception ignored) {}
                try { user.setPhone(rs.getString("phone")); } catch (Exception ignored) {}
                try { user.setAddress(rs.getString("address")); } catch (Exception ignored) {}
                try { user.setEmailVerified(rs.getBoolean("email_verified")); } catch (Exception ignored) {}
                try { user.setMustChangePassword(rs.getBoolean("must_change_password")); } catch (Exception ignored) {}
                user.setStatus(rs.getString("status"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) user.setCreatedAt(createdAt.toInstant());
                int teacherId = rs.getInt("teacher_id");
                user.setTeacherId(rs.wasNull() ? null : teacherId);
                int studentId = rs.getInt("student_id");
                user.setStudentId(rs.wasNull() ? null : studentId);
            }
            String roleCode = rs.getString("role_code");
            if (roleCode != null && !roleCode.isBlank()) roles.add(roleCode);
        }
        if (user == null) return null;
        for (String r : roles) user.addRoleCode(r);
        return user;
    }

    public void updateProfile(int userId, String fullName, String email, String phone, String address) throws Exception {
        String sql = """
                UPDATE dbo.users
                SET full_name = NULLIF(?, N''),
                    email = NULLIF(?, N''),
                    phone = NULLIF(?, N''),
                    address = NULLIF(?, N'')
                WHERE user_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName == null ? "" : fullName.trim());
            ps.setString(2, email == null ? "" : email.trim());
            ps.setString(3, phone == null ? "" : phone.trim());
            ps.setString(4, address == null ? "" : address.trim());
            ps.setInt(5, userId);
            ps.executeUpdate();
        }
    }

    public void clearEmailVerification(int userId) throws Exception {
        String sql = """
                UPDATE dbo.users
                SET email_verified = 0,
                    email_verify_token = NULL,
                    email_verify_expires = NULL
                WHERE user_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void issueEmailVerificationToken(int userId, String token, java.time.Instant expiresAt) throws Exception {
        String sql = """
                UPDATE dbo.users
                SET email_verified = 0,
                    email_verify_token = ?,
                    email_verify_expires = ?
                WHERE user_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, expiresAt == null ? null : java.sql.Timestamp.from(expiresAt));
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public Integer verifyEmailByToken(String token) throws Exception {
        if (token == null || token.isBlank()) return null;
        String sql = """
                UPDATE dbo.users
                SET email_verified = 1,
                    email_verify_token = NULL,
                    email_verify_expires = NULL
                WHERE email_verify_token = ?
                  AND (email_verify_expires IS NULL OR email_verify_expires >= SYSUTCDATETIME())
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token.trim());
            int affected = ps.executeUpdate();
            return affected > 0 ? 1 : null;
        }
    }
}
