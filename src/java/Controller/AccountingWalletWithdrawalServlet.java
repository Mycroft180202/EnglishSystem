package Controller;

import DAO.WalletWithdrawalDAO;
import DAO.WalletWithdrawalDAO.WithdrawalRow;
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
import java.util.List;

@WebServlet("/accounting/wallet-withdrawals")
public class AccountingWalletWithdrawalServlet extends HttpServlet {
    private final WalletWithdrawalDAO withdrawalDAO = new WalletWithdrawalDAO();
    private static final String TOKEN_KEY = "acctWithdrawToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            List<WithdrawalRow> rows = withdrawalDAO.listPending();
            req.setAttribute("rows", rows);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/accounting/withdrawal_request_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/accounting/wallet-withdrawals");
                return;
            }

            int requestId = parseInt(req.getParameter("requestId"), -1);
            String action = trim(req.getParameter("action"));
            if (requestId <= 0 || action.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/accounting/wallet-withdrawals");
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer decidedBy = actor == null ? null : actor.getUserId();

            if ("approve".equalsIgnoreCase(action)) {
                try {
                    boolean ok = withdrawalDAO.approveAndDebit(requestId, decidedBy);
                    if (ok) Flash.success(req, "Đã duyệt và trừ tiền trong ví.");
                    else Flash.error(req, "Không thể duyệt yêu cầu (có thể đã được xử lý).");
                } catch (IllegalStateException ex) {
                    if ("INSUFFICIENT_BALANCE".equals(ex.getMessage())) {
                        Flash.error(req, "Số dư ví không đủ để rút.");
                    } else {
                        throw ex;
                    }
                }
            } else if ("reject".equalsIgnoreCase(action)) {
                boolean ok = withdrawalDAO.reject(requestId, decidedBy, trim(req.getParameter("note")));
                if (ok) Flash.success(req, "Đã từ chối yêu cầu.");
                else Flash.error(req, "Không thể từ chối yêu cầu (có thể đã được xử lý).");
            }

            resp.sendRedirect(req.getContextPath() + "/accounting/wallet-withdrawals");
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

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

