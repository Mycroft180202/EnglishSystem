package Controller;

import DAO.StudentDAO;
import DAO.TeacherDAO;
import DAO.UserDAO;
import Model.User;
import Util.Flash;
import Util.FormToken;
import Util.MailUtil;
import Util.PasswordUtil;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;

@WebServlet("/app/profile")
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    private static final String TOKEN_INFO = "profileInfoToken";
    private static final String TOKEN_PWD = "profilePwdToken";
    private static final String TOKEN_EMAIL = "profileEmailToken";
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Flash.consume(req);

            User sessionUser = SecurityUtil.currentUser(req);
            if (sessionUser == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            User fresh = userDAO.findById(sessionUser.getUserId());
            if (fresh == null) {
                resp.sendRedirect(req.getContextPath() + "/logout");
                return;
            }

            req.setAttribute("u", fresh);
            req.setAttribute("infoToken", FormToken.issue(req, TOKEN_INFO));
            req.setAttribute("pwdToken", FormToken.issue(req, TOKEN_PWD));
            req.setAttribute("emailToken", FormToken.issue(req, TOKEN_EMAIL));
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            User sessionUser = SecurityUtil.currentUser(req);
            if (sessionUser == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            String action = trim(req.getParameter("action"));
            if (action.isEmpty()) action = "updateInfo";

            if ("updateInfo".equalsIgnoreCase(action)) {
                if (!FormToken.consume(req, TOKEN_INFO, req.getParameter("infoToken"))) {
                    resp.sendRedirect(req.getContextPath() + "/app/profile");
                    return;
                }
                handleUpdateInfo(req, resp, sessionUser);
                return;
            }

            if ("changePassword".equalsIgnoreCase(action)) {
                if (!FormToken.consume(req, TOKEN_PWD, req.getParameter("pwdToken"))) {
                    resp.sendRedirect(req.getContextPath() + "/app/profile");
                    return;
                }
                handleChangePassword(req, resp, sessionUser);
                return;
            }

            if ("sendVerify".equalsIgnoreCase(action)) {
                if (!FormToken.consume(req, TOKEN_EMAIL, req.getParameter("emailToken"))) {
                    resp.sendRedirect(req.getContextPath() + "/app/profile");
                    return;
                }
                handleSendVerify(req, resp, sessionUser);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/app/profile");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void handleUpdateInfo(HttpServletRequest req, HttpServletResponse resp, User sessionUser) throws Exception {
        User before = userDAO.findById(sessionUser.getUserId());
        if (before == null) {
            resp.sendRedirect(req.getContextPath() + "/logout");
            return;
        }

        String fullName = trim(req.getParameter("fullName"));
        String email = trimToNull(req.getParameter("email"));
        String phone = trimToNull(req.getParameter("phone"));
        String address = trimToNull(req.getParameter("address"));

        if (fullName.isEmpty()) {
            Flash.error(req, "Vui lòng nhập họ tên đầy đủ.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (fullName.length() > 150) {
            Flash.error(req, "Họ tên tối đa 150 ký tự.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (email != null && email.length() > 255) {
            Flash.error(req, "Email tối đa 255 ký tự.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (phone != null && phone.length() > 30) {
            Flash.error(req, "Số điện thoại tối đa 30 ký tự.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (address != null && address.length() > 255) {
            Flash.error(req, "Địa chỉ tối đa 255 ký tự.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }

        boolean emailChanged = !equalsIgnoreCaseNullable(before.getEmail(), email);

        try {
            userDAO.updateProfile(before.getUserId(), fullName, email, phone, address);
            if (emailChanged) userDAO.clearEmailVerification(before.getUserId());

            if (before.getStudentId() != null) {
                studentDAO.updateContact(before.getStudentId(), fullName, email, phone, address);
            }
            if (before.getTeacherId() != null) {
                teacherDAO.updateContact(before.getTeacherId(), fullName, email, phone);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            Flash.error(req, "Email hoặc số điện thoại đã được sử dụng bởi tài khoản khác.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        } catch (Exception ex) {
            // SQL Server often throws SQLServerException for unique index violations; keep message generic.
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("unique") || msg.contains("duplicate")) {
                Flash.error(req, "Email hoặc số điện thoại đã được sử dụng bởi tài khoản khác.");
                resp.sendRedirect(req.getContextPath() + "/app/profile");
                return;
            }
            throw ex;
        }

        // Refresh session user
        User fresh = userDAO.findById(before.getUserId());
        HttpSession session = req.getSession(false);
        if (session != null && fresh != null) session.setAttribute(SecurityUtil.SESSION_USER, fresh);

        Flash.success(req, "Đã cập nhật thông tin cá nhân.");
        resp.sendRedirect(req.getContextPath() + "/app/profile");
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, User sessionUser) throws Exception {
        User before = userDAO.findById(sessionUser.getUserId());
        if (before == null) {
            resp.sendRedirect(req.getContextPath() + "/logout");
            return;
        }

        String current = req.getParameter("currentPassword");
        String newPwd = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        if (current == null || current.isBlank() || newPwd == null || newPwd.isBlank() || confirm == null || confirm.isBlank()) {
            Flash.error(req, "Vui lòng nhập đầy đủ mật khẩu hiện tại và mật khẩu mới.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (!newPwd.equals(confirm)) {
            Flash.error(req, "Xác nhận mật khẩu không khớp.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (newPwd.length() < 8) {
            Flash.error(req, "Mật khẩu mới tối thiểu 8 ký tự.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (!PasswordUtil.verifyPassword(current.toCharArray(), before.getPasswordHash())) {
            Flash.error(req, "Mật khẩu hiện tại không đúng.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }

        String hash = PasswordUtil.hashPassword(newPwd.toCharArray());
        userDAO.updatePassword(before.getUserId(), hash);

        User fresh = userDAO.findById(before.getUserId());
        HttpSession session = req.getSession(false);
        if (session != null && fresh != null) session.setAttribute(SecurityUtil.SESSION_USER, fresh);

        Flash.success(req, "Đã đổi mật khẩu.");
        resp.sendRedirect(req.getContextPath() + "/app/profile");
    }

    private void handleSendVerify(HttpServletRequest req, HttpServletResponse resp, User sessionUser) throws Exception {
        User u = userDAO.findById(sessionUser.getUserId());
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/logout");
            return;
        }
        if (u.getEmail() == null || u.getEmail().isBlank()) {
            Flash.error(req, "Bạn chưa nhập email để xác thực.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }
        if (u.isEmailVerified()) {
            Flash.success(req, "Email đã được xác thực trước đó.");
            resp.sendRedirect(req.getContextPath() + "/app/profile");
            return;
        }

        String token = randomTokenHex(24);
        Instant expires = Instant.now().plusSeconds(30 * 60);
        userDAO.issueEmailVerificationToken(u.getUserId(), token, expires);

        String verifyUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath() + "/app/verify-email?token=" + token;
        try {
            MailUtil.sendEmailVerification(u.getEmail(), verifyUrl);
            Flash.success(req, "Đã gửi email xác thực. Vui lòng kiểm tra email.");
        } catch (Exception ex) {
            Flash.error(req, "Gửi email thất bại. Vui lòng kiểm tra cấu hình Gmail SMTP (MailUtil) hoặc thử lại sau.");
        }
        resp.sendRedirect(req.getContextPath() + "/app/profile");
    }

    private static boolean equalsIgnoreCaseNullable(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private static String randomTokenHex(int bytes) {
        byte[] b = new byte[Math.max(8, bytes)];
        RNG.nextBytes(b);
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trimToNull(String s) {
        String t = trim(s);
        return t.isEmpty() ? null : t;
    }
}
