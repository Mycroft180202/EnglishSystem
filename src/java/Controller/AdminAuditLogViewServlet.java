package Controller;

import DAO.AuditLogDAO;
import Model.AuditLog;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/audit-logs/view")
public class AdminAuditLogViewServlet extends HttpServlet {
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            long id = parseLong(req.getParameter("id"), -1);
            if (id <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/audit-logs");
                return;
            }

            AuditLog log = auditLogDAO.findById(id);
            if (log == null) {
                Flash.error(req, "Audit log not found.");
                resp.sendRedirect(req.getContextPath() + "/admin/audit-logs");
                return;
            }

            req.setAttribute("log", log);
            req.getRequestDispatcher("/WEB-INF/views/admin/audit_log_view.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }
}

