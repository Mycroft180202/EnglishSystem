package Controller;

import DAO.WalletWithdrawalDAO;
import Model.User;
import Util.Flash;
import Util.FormToken;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/consultant/wallet-withdrawals/create")
public class ConsultantWalletWithdrawalCreateServlet extends HttpServlet {
    private final WalletWithdrawalDAO withdrawalDAO = new WalletWithdrawalDAO();
    private static final String TOKEN_KEY = "consultantWithdrawToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Flash.consume(req);
        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.getRequestDispatcher("/WEB-INF/views/consultant/withdrawal_create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals");
                return;
            }

            int studentId = parseInt(req.getParameter("studentId"), -1);
            BigDecimal amount = parseDecimal(req.getParameter("amount"));
            String note = trim(req.getParameter("note"));
            if (studentId <= 0 || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.error(req, "Thông tin không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals/create");
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer createdBy = actor == null ? null : actor.getUserId();
            withdrawalDAO.create(studentId, amount, note, createdBy);

            Flash.success(req, "Đã tạo yêu cầu rút tiền. Chờ kế toán duyệt.");
            resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static BigDecimal parseDecimal(String s) {
        if (s == null) return null;
        String t = s.trim().replace(",", "");
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

