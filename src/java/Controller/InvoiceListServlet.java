package Controller;

import DAO.InvoiceDAO;
import Model.Invoice;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/accounting/invoices")
public class InvoiceListServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            String status = trim(req.getParameter("status"));
            if (status.isEmpty()) status = null;
            String q = trim(req.getParameter("q"));
            if (q.isEmpty()) q = null;

            List<Invoice> invoices = invoiceDAO.listAll(status, q);
            req.setAttribute("invoices", invoices);
            req.setAttribute("status", status);
            req.setAttribute("q", q);
            req.getRequestDispatcher("/WEB-INF/views/accounting/invoice_list.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

