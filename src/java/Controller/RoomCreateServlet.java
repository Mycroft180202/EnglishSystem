package Controller;

import DAO.RoomDAO;
import Model.Room;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/rooms/create")
public class RoomCreateServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();
    private static final String TOKEN_KEY = "roomCreateToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
        req.setAttribute("mode", "create");
        req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/rooms");
                return;
            }

            Room r = read(req);
            String validation = validate(r);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("room", r);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
                return;
            }

            if (roomDAO.findByCode(r.getRoomCode()) != null) {
                req.setAttribute("error", "Mã phòng đã tồn tại.");
                req.setAttribute("room", r);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "create");
                req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
                return;
            }

            roomDAO.create(r);
            Flash.success(req, "Tạo phòng học thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/rooms");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static Room read(HttpServletRequest req) {
        Room r = new Room();
        r.setRoomCode(trim(req.getParameter("roomCode")));
        r.setRoomName(trim(req.getParameter("roomName")));
        r.setCapacity(parseInt(req.getParameter("capacity"), 0));
        String status = trim(req.getParameter("status"));
        r.setStatus(status.isEmpty() ? "ACTIVE" : status);
        return r;
    }

    private static String validate(Room r) {
        if (r.getRoomCode().isBlank()) return "Vui lòng nhập mã phòng.";
        if (r.getRoomCode().length() > 50) return "Mã phòng tối đa 50 ký tự.";
        if (r.getRoomName().isBlank()) return "Vui lòng nhập tên phòng.";
        if (r.getRoomName().length() > 100) return "Tên phòng tối đa 100 ký tự.";
        if (r.getCapacity() <= 0) return "Sức chứa phải > 0.";
        if (!("ACTIVE".equalsIgnoreCase(r.getStatus()) || "INACTIVE".equalsIgnoreCase(r.getStatus())))
            return "Trạng thái không hợp lệ.";
        return null;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

