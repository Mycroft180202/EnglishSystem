package Controller;

import DAO.WalletWithdrawalDAO;
import DAO.WalletWithdrawalDAO.WithdrawalRow;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/consultant/wallet-withdrawals")
public class ConsultantWalletWithdrawalListServlet extends HttpServlet {
    private final WalletWithdrawalDAO withdrawalDAO = new WalletWithdrawalDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            String status = trim(req.getParameter("status"));
            if (status.isBlank()) status = null;

            List<WithdrawalRow> rows = withdrawalDAO.listAll(status);
            req.setAttribute("rows", rows);
            req.setAttribute("status", status);
            req.getRequestDispatcher("/WEB-INF/views/consultant/withdrawal_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

