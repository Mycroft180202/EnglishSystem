package Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public final class FormToken {
    private FormToken() {}

    public static String issue(HttpServletRequest req, String key) {
        HttpSession session = req.getSession(true);
        String token = UUID.randomUUID().toString();
        session.setAttribute(key, token);
        return token;
    }

    public static boolean consume(HttpServletRequest req, String key, String provided) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Object stored = session.getAttribute(key);
        session.removeAttribute(key);
        if (!(stored instanceof String)) return false;
        if (provided == null) return false;
        return ((String) stored).equals(provided);
    }
}

