package Controller;

import DAO.InvoiceDAO;
import DAO.PaymentDAO;
import Model.Invoice;
import Model.Payment;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/accounting/invoices/view")
public class InvoiceViewServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            int id = parseInt(req.getParameter("id"), -1);
            Invoice invoice = id > 0 ? invoiceDAO.findById(id) : null;
            if (invoice == null) {
                resp.sendRedirect(req.getContextPath() + "/accounting/invoices");
                return;
            }

            List<Payment> payments = paymentDAO.listByInvoice(id);
            req.setAttribute("invoice", invoice);
            req.setAttribute("payments", payments);
            req.setAttribute("paymentFormToken", PaymentAddServlet.issueToken(req));
            req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_view.jsp").forward(req, resp);
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
