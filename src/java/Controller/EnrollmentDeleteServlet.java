package Controller;

import DAO.DeleteResult;
import DAO.EnrollmentDAO;
import DAO.EnrollmentDAO.RefundResult;
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
import Model.User;

@WebServlet("/admin/enrollments/delete")
public class EnrollmentDeleteServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int id = parseInt(req.getParameter("id"), -1);
            if (id <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/enrollments");
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer actorId = actor == null ? null : actor.getUserId();
            // Refund (to wallet) first, then delete enrollment + related records.
            RefundResult rr = enrollmentDAO.refundToWalletIfNeeded(id, actorId);
            if (!rr.ok) {
                Flash.error(req, rr.message);
                auditService.log(req, "DELETE", "ENROLLMENT", String.valueOf(id),
                        "{" + JsonUtil.kv("result", "FAIL") + "," + JsonUtil.kv("message", rr.message) + "}");
                resp.sendRedirect(req.getContextPath() + "/admin/enrollments");
                return;
            }

            DeleteResult r = enrollmentDAO.deleteCascade(id);
            if (r.ok) Flash.success(req, r.message);
            else Flash.error(req, r.message);

            auditService.log(req, "DELETE", "ENROLLMENT", String.valueOf(id),
                    "{" + JsonUtil.kv("result", r.ok ? "SUCCESS" : "FAIL") + "," + JsonUtil.kv("message", r.message) + "}");

            resp.sendRedirect(req.getContextPath() + "/admin/enrollments");
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
