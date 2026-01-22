package Controller;

import DAO.ClassDAO;
import DAO.EnrollmentDAO;
import DAO.WalletDAO;
import Model.CenterClass;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/student/classes")
public class StudentClassBrowseServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User user = SecurityUtil.currentUser(req);
            if (user == null || user.getStudentId() == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.getRequestDispatcher("/WEB-INF/views/error_403.jsp").forward(req, resp);
                return;
            }

            List<CenterClass> classes = classDAO.listAll("OPEN", null);
            BigDecimal balance = walletDAO.getBalance(user.getStudentId());
            req.setAttribute("classes", classes);
            req.setAttribute("balance", balance);
            req.getRequestDispatcher("/WEB-INF/views/student/classes.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
