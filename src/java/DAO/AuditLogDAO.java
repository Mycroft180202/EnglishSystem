package DAO;

import Model.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO extends DBContext {
    public void insert(Integer actorUserId, String action, String entity, String entityId, String dataJson, String ip) throws Exception {
        String sql = """
                INSERT INTO dbo.audit_logs(actor_user_id, action, entity, entity_id, data_json, ip)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (actorUserId == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, actorUserId);
            ps.setString(2, action);
            ps.setString(3, entity);
            ps.setString(4, entityId);
            ps.setString(5, dataJson);
            ps.setString(6, ip);
            ps.executeUpdate();
        }
    }

    public AuditLog findById(long auditId) throws Exception {
        String sql = """
                SELECT a.audit_id, a.actor_user_id, u.username AS actor_username,
                       a.action, a.entity, a.entity_id, a.data_json, a.created_at, a.ip
                FROM dbo.audit_logs a
                LEFT JOIN dbo.users u ON a.actor_user_id = u.user_id
                WHERE a.audit_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, auditId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public List<AuditLog> listRecent(int limit) throws Exception {
        String sql = """
                SELECT TOP (?) a.audit_id, a.actor_user_id, u.username AS actor_username,
                       a.action, a.entity, a.entity_id, a.data_json, a.created_at, a.ip
                FROM dbo.audit_logs a
                LEFT JOIN dbo.users u ON a.actor_user_id = u.user_id
                ORDER BY a.created_at DESC, a.audit_id DESC
                """;
        List<AuditLog> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 500)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    private static AuditLog map(ResultSet rs) throws Exception {
        AuditLog a = new AuditLog();
        a.setAuditId(rs.getLong("audit_id"));
        int actorId = rs.getInt("actor_user_id");
        a.setActorUserId(rs.wasNull() ? null : actorId);
        a.setActorUsername(rs.getString("actor_username"));
        a.setAction(rs.getString("action"));
        a.setEntity(rs.getString("entity"));
        a.setEntityId(rs.getString("entity_id"));
        a.setDataJson(rs.getString("data_json"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) a.setCreatedAt(t.toInstant());
        a.setIp(rs.getString("ip"));
        return a;
    }
}
