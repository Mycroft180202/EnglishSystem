package Util;

import Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Set;


public final class SecurityUtil {
    public static final String SESSION_USER = "authUser";

    private SecurityUtil() {}

    public static User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute(SESSION_USER);
        return (value instanceof User) ? (User) value : null;
    }

    public static boolean hasAnyRole(User user, String... roleCodes) {
        if (user == null) return false;
        Set<String> roles = user.getRoleCodes();
        if (roles == null || roles.isEmpty()) return false;
        for (String code : roleCodes) {
            if (roles.contains(code)) return true;
        }
        return false;
    }
}

