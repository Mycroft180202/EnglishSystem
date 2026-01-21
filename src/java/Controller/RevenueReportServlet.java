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
}

