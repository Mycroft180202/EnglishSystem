package Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Flash {
    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private Flash() {}

    public static void success(HttpServletRequest req, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_SUCCESS, message);
    }

    public static void error(HttpServletRequest req, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_ERROR, message); 
    }

    public static void consume(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return;

        Object ok = session.getAttribute(FLASH_SUCCESS);
        Object err = session.getAttribute(FLASH_ERROR);
        if (ok != null) req.setAttribute(FLASH_SUCCESS, ok);
        if (err != null) req.setAttribute(FLASH_ERROR, err);
        session.removeAttribute(FLASH_SUCCESS);
        session.removeAttribute(FLASH_ERROR);
    }
}

