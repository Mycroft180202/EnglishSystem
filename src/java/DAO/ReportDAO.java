package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO extends DBContext {
    public static final class RevenueRow {
        public String day; // yyyy-MM-dd
        public BigDecimal total;
        public int paymentCount;
    }

    public List<RevenueRow> revenueByDay(YearMonth month) throws Exception {
        String sql = """
                SELECT CONVERT(varchar(10), p.paid_at, 120) AS day,
                       SUM(p.amount) AS total,
                       COUNT(*) AS payment_count
                FROM dbo.payments p
                WHERE p.paid_at >= ? AND p.paid_at < ?
                GROUP BY CONVERT(varchar(10), p.paid_at, 120)
                ORDER BY day
                """;
        List<RevenueRow> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, month.atDay(1).atStartOfDay().toString().replace('T', ' ') + ":00");
            ps.setString(2, month.plusMonths(1).atDay(1).atStartOfDay().toString().replace('T', ' ') + ":00");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueRow r = new RevenueRow();
                    r.day = rs.getString("day");
                    r.total = rs.getBigDecimal("total");
                    r.paymentCount = rs.getInt("payment_count");
                    result.add(r);
                }
            }
        }
        return result;
    }
}

