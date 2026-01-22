package Controller;

import DAO.StudentFeeDAO;
import Model.Invoice;
import Model.User;
import Util.Flash;
import Util.SecurityUtil;
import Service.PayOsSyncService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/student/fees")
public class StudentFeeServlet extends HttpServlet {
    private final StudentFeeDAO feeDAO = new StudentFeeDAO();
    private final PayOsSyncService payOsSync = new PayOsSyncService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            // If returning from PayOS, reconcile payment immediately (webhook may not reach localhost).
            String orderCode = trim(req.getParameter("orderCode"));
            String status = trim(req.getParameter("status"));
            String code = trim(req.getParameter("code"));
            String cancel = trim(req.getParameter("cancel"));
            String id = trim(req.getParameter("id"));
            if (!orderCode.isEmpty() || !id.isEmpty()) {
                PayOsSyncService.SyncResult r = payOsSync.syncByReturnParams(orderCode, id, status, code, cancel);
                if (r.updated) Flash.success(req, "Thanh toán PayOS đã được ghi nhận.");
                else if (r.paid) Flash.success(req, "Thanh toán PayOS đã ghi nhận (đã cập nhật trước đó).");
                else Flash.error(req, "Thanh toán PayOS chưa được ghi nhận: " + r.message);
                resp.sendRedirect(req.getContextPath() + "/student/fees");
                return;
            }

            List<Invoice> invoices = feeDAO.listInvoicesByStudent(user.getStudentId());
            req.setAttribute("invoices", invoices);
            req.getRequestDispatcher("/WEB-INF/views/student/fees.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
