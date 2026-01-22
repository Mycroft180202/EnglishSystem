package Controller;

import DAO.AuditLogDAO;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/audit-logs")
public class AdminAuditLogServlet extends HttpServlet {
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Flash.consume(req);
            req.setAttribute("logs", auditLogDAO.listRecent(200));
            req.getRequestDispatcher("/WEB-INF/views/admin/audit_log_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}

