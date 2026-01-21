package Controller;

import DAO.TimeSlotDAO;
import Model.TimeSlot;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalTime;

@WebServlet("/admin/time-slots/edit")
public class TimeSlotEditServlet extends HttpServlet {
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private static final String TOKEN_KEY = "timeSlotEditToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int id = parseInt(req.getParameter("id"), -1);
            TimeSlot slot = id > 0 ? timeSlotDAO.findById(id) : null;
            if (slot == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
                return;
            }

            req.setAttribute("slot", slot);
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher("/WEB-INF/views/admin/time_slot_form.jsp").forward(req, resp);
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
                resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
                return;
            }

            int id = parseInt(req.getParameter("slotId"), -1);
            TimeSlot existing = id > 0 ? timeSlotDAO.findById(id) : null;
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
                return;
            }

            TimeSlot s = read(req);
            s.setSlotId(id);
            String validation = validate(s);
            if (validation != null) {
                req.setAttribute("error", validation);
                req.setAttribute("slot", s);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/time_slot_form.jsp").forward(req, resp);
                return;
            }

            try {
                timeSlotDAO.update(s);
            } catch (Exception ex) {
                req.setAttribute("error", "Khung giờ đã tồn tại (start/end trùng).");
                req.setAttribute("slot", s);
                req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/views/admin/time_slot_form.jsp").forward(req, resp);
                return;
            }

            Flash.success(req, "Cập nhật khung giờ thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/time-slots");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static TimeSlot read(HttpServletRequest req) {
        TimeSlot s = new TimeSlot();
        s.setName(trim(req.getParameter("name")));
        s.setStartTime(parseTime(req.getParameter("startTime")));
        s.setEndTime(parseTime(req.getParameter("endTime")));
        return s;
    }

    private static String validate(TimeSlot s) {
        if (s.getName().isBlank()) return "Vui lòng nhập tên ca.";
        if (s.getName().length() > 50) return "Tên ca tối đa 50 ký tự.";
        if (s.getStartTime() == null || s.getEndTime() == null) return "Vui lòng chọn giờ bắt đầu/kết thúc.";
        if (!s.getStartTime().isBefore(s.getEndTime())) return "Giờ bắt đầu phải nhỏ hơn giờ kết thúc.";
        return null;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static LocalTime parseTime(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return LocalTime.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

