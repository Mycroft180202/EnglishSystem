package Filter;

import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthzFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        User user = SecurityUtil.currentUser(req);
        if (user == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        String[] requiredRoles = requiredRolesForPath(path);
        if (requiredRoles != null && !SecurityUtil.hasAnyRole(user, requiredRoles)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    private static boolean isPublicPath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) return true;
        if (path.startsWith("/assets/")) return true;
        return path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/logout")
                || path.equals("/register")
                || path.equals("/setup");
    }

    private static String[] requiredRolesForPath(String path) {
        if (path.startsWith("/admin/")) return new String[]{"ADMIN"};
        if (path.startsWith("/accounting/")) return new String[]{"ADMIN", "ACCOUNTANT"};
        if (path.startsWith("/consultant/")) return new String[]{"ADMIN", "CONSULTANT"};
        if (path.startsWith("/teacher/")) return new String[]{"ADMIN", "TEACHER"};
        if (path.startsWith("/student/")) return new String[]{"ADMIN", "STUDENT"};
        if (path.startsWith("/app/")) return null; // any logged-in user
        return null;
    }
}
