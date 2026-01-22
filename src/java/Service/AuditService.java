package Service;

import DAO.AuditLogDAO;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;

public class AuditService {
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public void log(HttpServletRequest req, String action, String entity, String entityId, String dataJson) {
        try {
            User actor = SecurityUtil.currentUser(req);
            Integer actorUserId = actor == null ? null : actor.getUserId();
            auditLogDAO.insert(actorUserId, action, entity, entityId, dataJson, clientIp(req));
        } catch (Exception ignored) {
            // Audit logging should not break business flow.
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        return req.getRemoteAddr();
    }
}

