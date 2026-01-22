package Controller;

import DAO.ClassDAO;
import DAO.DeleteResult;
import Service.AuditService;
import Util.Flash;
import Util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/classes/delete")
public class ClassDeleteServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int id = parseInt(req.getParameter("id"), -1);
            if (id <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            DeleteResult r = classDAO.deleteCascade(id);
            if (r.ok) Flash.success(req, r.message);
            else Flash.error(req, r.message);

            auditService.log(req, "DELETE", "CLASS", String.valueOf(id),
                    "{" + JsonUtil.kv("result", r.ok ? "SUCCESS" : "FAIL") + "," + JsonUtil.kv("message", r.message) + "}");

            resp.sendRedirect(req.getContextPath() + "/admin/classes");
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

