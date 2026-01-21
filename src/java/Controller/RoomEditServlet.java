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

@WebServlet("/admin/rooms/edit")
public class RoomEditServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();
    private static final String TOKEN_KEY = "roomEditToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            Room r = id > 0 ? roomDAO.findById(id) : null;
            if (r == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/rooms");
                return;
            }

            req.setAttribute("room", r);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
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

            int id = parseInt(req.getParameter("roomId"), -1);
            Room existing = id > 0 ? roomDAO.findById(id) : null;
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/rooms");
                return;
            }

            Room r = new Room();
            r.setRoomId(id);
            r.setRoomCode(trim(req.getParameter("roomCode")));
            r.setRoomName(trim(req.getParameter("roomName")));
            r.setCapacity(parseInt(req.getParameter("capacity"), 0));
            String status = trim(req.getParameter("status"));
            r.setStatus(status.isEmpty() ? "ACTIVE" : status);

            String validation = validate(r);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("room", r);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
                return;
            }

            Room byCode = roomDAO.findByCode(r.getRoomCode());
            if (byCode != null && byCode.getRoomId() != r.getRoomId()) {
                req.setAttribute("error", "Mã phòng đã tồn tại.");
                req.setAttribute("room", r);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/room_form.jsp").forward(req, resp);
                return;
            }

            roomDAO.update(r);
            Flash.success(req, "Cập nhật phòng học thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/rooms");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
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

