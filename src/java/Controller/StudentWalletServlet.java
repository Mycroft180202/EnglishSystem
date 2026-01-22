package Controller;

import DAO.WalletDAO;
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

@WebServlet("/student/wallet")
public class StudentWalletServlet extends HttpServlet {
    private final WalletDAO walletDAO = new WalletDAO();
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

            String orderCode = trim(req.getParameter("orderCode"));
            String status = trim(req.getParameter("status"));
            String code = trim(req.getParameter("code"));
            String cancel = trim(req.getParameter("cancel"));
            String id = trim(req.getParameter("id"));
            if (!orderCode.isEmpty() || !id.isEmpty()) {
                PayOsSyncService.SyncResult r = payOsSync.syncByReturnParams(orderCode, id, status, code, cancel);
                if (r.updated) Flash.success(req, "Nạp ví PayOS đã được ghi nhận.");
                else if (r.paid) Flash.success(req, "Nạp ví PayOS đã ghi nhận (đã cập nhật trước đó).");
                else Flash.error(req, "Nạp ví PayOS chưa được ghi nhận: " + r.message);
                resp.sendRedirect(req.getContextPath() + "/student/wallet");
                return;
            }

            req.setAttribute("balance", walletDAO.getBalance(user.getStudentId()));
            req.getRequestDispatcher("/WEB-INF/views/student/wallet.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write("Use /student/wallet/topup/payos to top up wallet.");
        return;

        /* legacy manual topup (disabled)
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/student/wallet");
                return;
            }

            BigDecimal amount = parseDecimal(req.getParameter("amount"));
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.error(req, "Số tiền không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/student/wallet");
                return;
            }

            walletDAO.topup(user.getStudentId(), amount, user.getUserId(), "Student topup");
            Flash.success(req, "Nạp tiền vào ví thành công.");
            resp.sendRedirect(req.getContextPath() + "/student/wallet");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
        */
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
