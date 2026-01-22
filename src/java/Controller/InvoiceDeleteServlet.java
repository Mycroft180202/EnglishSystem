package Controller;

import DAO.DeleteResult;
import DAO.InvoiceDAO;
import Model.User;
import Service.AuditService;
import Util.Flash;
import Util.JsonUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/accounting/invoices/delete")
public class InvoiceDeleteServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = SecurityUtil.currentUser(req);
            if (!SecurityUtil.hasAnyRole(user, "ADMIN")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            int id = parseInt(req.getParameter("id"), -1);
            if (id <= 0) {
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices");
                return;
            }

            DeleteResult r = invoiceDAO.deleteCascade(id);
            if (r.ok) Flash.success(req, r.message);
            else Flash.error(req, r.message);

            auditService.log(req, "DELETE", "INVOICE", String.valueOf(id),
                    "{" + JsonUtil.kv("result", r.ok ? "SUCCESS" : "FAIL") + "," + JsonUtil.kv("message", r.message) + "}");

            resp.sendRedirect(req.getContextPath() + "/accounting/invoices");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

