package Controller;

import DAO.EnrollmentDAO;
import DAO.EnrollmentDAO.EnrollPayInfo;
import DAO.EnrollmentDAO.RefundResult;
import Model.User;
import Util.Flash;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/admin/enrollments/status", "/consultant/enrollments/status"})
public class EnrollmentStatusServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            String status = trim(req.getParameter("status")).toUpperCase();
            if (id <= 0 || !isValid(status)) {
                Flash.error(req, "Thao tác không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
                return;
            }

            if ("ACTIVE".equals(status)) {
                EnrollPayInfo info = enrollmentDAO.findPayInfo(id);
                if (info == null) {
                    Flash.error(req, "Không tìm thấy đăng ký học.");
                    resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
                    return;
                }
                if (info.getInvoiceId() != null && (info.getInvoiceStatus() == null || !"PAID".equalsIgnoreCase(info.getInvoiceStatus()))) {
                    Flash.error(req, "Chưa thể duyệt vào lớp vì học phí chưa được thanh toán đầy đủ.");
                    resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
                    return;
                }
            }

            User actor = SecurityUtil.currentUser(req);
            Integer actorId = actor == null ? null : actor.getUserId();

            if ("CANCELLED".equals(status)) {
                RefundResult rr = enrollmentDAO.cancelWithRefundToWallet(id, actorId);
                if (rr.ok) Flash.success(req, rr.message);
                else Flash.error(req, rr.message);
                resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
                return;
            }

            enrollmentDAO.setStatus(id, status);
            Flash.success(req, "Cập nhật trạng thái đăng ký học thành công.");
            resp.sendRedirect(req.getContextPath() + basePath(req) + "/enrollments");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static boolean isValid(String s) {
        return "ACTIVE".equals(s) || "CANCELLED".equals(s) || "COMPLETED".equals(s) || "PENDING".equals(s);
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static String basePath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        return path.startsWith("/admin/") ? "/admin" : "/consultant";
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
