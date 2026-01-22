package Controller;

import DAO.ReportDAO;
import DAO.ReportDAO.RevenueRow;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

@WebServlet("/accounting/reports/revenue")
public class RevenueReportServlet extends HttpServlet {
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            YearMonth ym = parseYearMonth(req.getParameter("month"));
            if (ym == null) ym = YearMonth.now();

            List<RevenueRow> rows = reportDAO.revenueByDay(ym);

            if ("1".equals(trim(req.getParameter("export")))) {
                exportCsv(resp, ym, rows);
                return;
            }

            req.setAttribute("month", ym.toString()); // yyyy-MM
            req.setAttribute("rows", rows);
            req.getRequestDispatcher("/WEB-INF/views/accounting/revenue_report.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static YearMonth parseYearMonth(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return YearMonth.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static void exportCsv(HttpServletResponse resp, YearMonth ym, List<RevenueRow> rows) throws IOException {
        String filename = "revenue_" + ym + ".csv";
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter out = resp.getWriter()) {
            out.write('\uFEFF'); // UTF-8 BOM for Excel
            out.println("Day,PaymentCount,Total");
            for (RevenueRow r : rows) {
                String day = r.day == null ? "" : r.day;
                String cnt = String.valueOf(r.paymentCount);
                String total = r.total == null ? "0" : r.total.toPlainString();
                out.println(day + "," + cnt + "," + total);
            }
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
