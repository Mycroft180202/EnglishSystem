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
                SELECT u.user_id, u.username, u.password_hash, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
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
                SELECT u.user_id, u.username, u.password_hash, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
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
        String insertUserSql = "INSERT INTO dbo.users(username, password_hash, status) VALUES(?, ?, N'ACTIVE')";
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
        String insertUserSql = "INSERT INTO dbo.users(username, password_hash, teacher_id, status) VALUES(?, ?, ?, N'ACTIVE')";
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
        String insertUserSql = "INSERT INTO dbo.users(username, password_hash, student_id, status) VALUES(?, ?, ?, N'ACTIVE')";
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
                SELECT u.user_id, u.username, u.password_hash, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
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
                SELECT u.user_id, u.username, u.password_hash, u.status, u.created_at, u.teacher_id, u.student_id, r.role_code
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
        String sql = "UPDATE dbo.users SET password_hash = ? WHERE user_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
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
}
