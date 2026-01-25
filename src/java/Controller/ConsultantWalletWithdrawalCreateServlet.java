package Controller;

import DAO.StudentDAO;
import DAO.WalletDAO;
import DAO.WalletWithdrawalDAO;
import DAO.WalletWithdrawalDAO.StudentOption;
import Model.Student;
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
import java.util.List;

@WebServlet("/consultant/wallet-withdrawals/create")
public class ConsultantWalletWithdrawalCreateServlet extends HttpServlet {
    private final WalletWithdrawalDAO withdrawalDAO = new WalletWithdrawalDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private static final String TOKEN_KEY = "consultantWithdrawToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            String q = trim(req.getParameter("q"));
            if (q.isBlank()) q = null;
            List<StudentOption> students = withdrawalDAO.listActiveStudentsWithBalance(q);
            req.setAttribute("students", students);
            req.setAttribute("q", q);
            req.setAttribute("selectedStudentId", parseInt(req.getParameter("studentId"), -1));

            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/consultant/withdrawal_create.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals");
                return;
            }

            int studentId = parseInt(req.getParameter("studentId"), -1);
            String mode = trim(req.getParameter("mode"));
            if (mode.isBlank()) mode = "CUSTOM";

            BigDecimal balance = (studentId > 0) ? walletDAO.getBalance(studentId) : BigDecimal.ZERO;
            if (balance == null) balance = BigDecimal.ZERO;

            BigDecimal amount;
            if ("ALL".equalsIgnoreCase(mode)) {
                amount = balance;
            } else {
                amount = parseDecimal(req.getParameter("amount"));
            }

            String note = trim(req.getParameter("note"));
            if (studentId <= 0) {
                Flash.error(req, "Vui lòng chọn học viên.");
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals/create");
                return;
            }

            Student st = studentDAO.findById(studentId);
            if (st == null || !"ACTIVE".equalsIgnoreCase(st.getStatus())) {
                Flash.error(req, "Học viên không hợp lệ hoặc đã ngưng hoạt động.");
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals/create");
                return;
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Flash.error(req, "Số tiền rút không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals/create?studentId=" + studentId);
                return;
            }
            if (amount.compareTo(balance) > 0) {
                Flash.error(req, "Số dư ví không đủ để tạo yêu cầu rút tiền.");
                resp.sendRedirect(req.getContextPath() + "/consultant/wallet-withdrawals/create?studentId=" + studentId);
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
