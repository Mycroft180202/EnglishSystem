package Controller;

import DAO.EnrollmentDAO;
import DAO.PaymentDAO;
import DAO.PaymentRequestDAO;
import DAO.PaymentRequestDAO.PaymentRequestRow;
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

@WebServlet("/accounting/payment-requests")
public class AccountingPaymentRequestServlet extends HttpServlet {
    private final PaymentRequestDAO requestDAO = new PaymentRequestDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private static final String TOKEN_KEY = "acctPayReqToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            List<PaymentRequestRow> rows = requestDAO.listPending();
            req.setAttribute("rows", rows);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/accounting/payment_request_list.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/accounting/payment-requests");
                return;
            }

            int requestId = parseInt(req.getParameter("requestId"), -1);
            String action = trim(req.getParameter("action"));
            if (requestId <= 0 || action.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/accounting/payment-requests");
                return;
            }

            PaymentRequestRow r = requestDAO.findById(requestId);
            if (r == null || !"PENDING".equalsIgnoreCase(r.getStatus())) {
                resp.sendRedirect(req.getContextPath() + "/accounting/payment-requests");
                return;
            }

            User actor = SecurityUtil.currentUser(req);
            Integer decidedBy = actor == null ? null : actor.getUserId();

            if ("approve".equalsIgnoreCase(action)) {
                boolean ok = requestDAO.markApproved(requestId, decidedBy);
                if (ok) {
                    try {
                        paymentDAO.addPayment(r.getInvoiceId(), r.getAmount(), r.getMethod(), "REQ#" + requestId, decidedBy);
                    } catch (IllegalStateException ex) {
                        requestDAO.markRejectedForce(requestId, decidedBy, "Auto rejected: already paid");
                        // Keep enrollment as PENDING; Admin/Consultant will approve the student into the class.
                        Flash.error(req, "Invoice already PAID. Request rejected.");
                        resp.sendRedirect(req.getContextPath() + "/accounting/payment-requests");
                        return;
                    }
                    // Keep enrollment as PENDING; Admin/Consultant will approve the student into the class.
                    Flash.success(req, "Đã duyệt và ghi nhận thanh toán.");
                }
            } else if ("reject".equalsIgnoreCase(action)) {
                requestDAO.markRejected(requestId, decidedBy);
                Flash.success(req, "Đã từ chối yêu cầu.");
            }

            resp.sendRedirect(req.getContextPath() + "/accounting/payment-requests");
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
