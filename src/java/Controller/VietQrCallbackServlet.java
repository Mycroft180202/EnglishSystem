package Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Disabled: migrated to PayOS webhook (/api/payos/webhook).
public class VietQrCallbackServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write("VietQR callback has been disabled. Use /api/payos/webhook instead.");
    }
}
