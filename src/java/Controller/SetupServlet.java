package Controller;

import DAO.UserDAO;
import Model.User;
import Util.PasswordUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/setup")
public class SetupServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (userDAO.anyUserExists()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            req.getRequestDispatcher("/WEB-INF/views/setup.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (userDAO.anyUserExists()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String username = trim(req.getParameter("username"));
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            if (username.isEmpty() || password == null || password.isBlank()) {
                req.setAttribute("error", "Vui lòng nhập username và password.");
                req.getRequestDispatcher("/WEB-INF/views/setup.jsp").forward(req, resp);
                return;
            }
            if (password.length() < 8) {
                req.setAttribute("error", "Password tối thiểu 8 ký tự.");
                req.getRequestDispatcher("/WEB-INF/views/setup.jsp").forward(req, resp);
                return;
            }
            if (!password.equals(confirm)) {
                req.setAttribute("error", "Xác nhận password không khớp.");
                req.getRequestDispatcher("/WEB-INF/views/setup.jsp").forward(req, resp);
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password.toCharArray());
            int userId = userDAO.createUser(username, passwordHash);
            userDAO.assignRoleByCode(userId, "ADMIN");

            User user = userDAO.findById(userId);
            HttpSession session = req.getSession(true);
            session.setAttribute(SecurityUtil.SESSION_USER, user);

            resp.sendRedirect(req.getContextPath() + "/app/home");
        } catch (Exception ex) {
            req.setAttribute("error", "Không thể tạo admin: " + ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/setup.jsp").forward(req, resp);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
