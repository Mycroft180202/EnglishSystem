package Controller;

import DAO.ClassDAO;
import DAO.ClassSessionDAO;
import DAO.EnrollmentDAO;
import DAO.RoomDAO;
import Model.CenterClass;
import Model.Room;
import Util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/class-sessions/update")
public class ClassSessionUpdateServlet extends HttpServlet {
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int sessionId = parseInt(req.getParameter("sessionId"), -1);
            int classId = parseInt(req.getParameter("classId"), -1);
            String action = trim(req.getParameter("action"));

            Integer actualClassId = sessionId > 0 ? sessionDAO.findClassIdBySessionId(sessionId) : null;
            if (actualClassId == null) {
                Flash.error(req, "Buổi học không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }
            if (classId <= 0) classId = actualClassId;

            if (!actualClassId.equals(classId)) {
                Flash.error(req, "Buổi học không thuộc lớp đã chọn.");
                resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                return;
            }

            if ("cancel".equalsIgnoreCase(action)) {
                String err = sessionDAO.cancelSession(sessionId);
                if (err == null) Flash.success(req, "Đã hủy buổi học.");
                else Flash.error(req, err);
                resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                return;
            }

            if ("reschedule".equalsIgnoreCase(action)) {
                LocalDate newDate = parseDate(req.getParameter("newDate"));
                int newSlotId = parseInt(req.getParameter("newSlotId"), 0);
                int newRoomId = parseInt(req.getParameter("newRoomId"), 0);

                CenterClass clazz = classDAO.findById(classId);
                if (clazz == null) {
                    Flash.error(req, "Lớp không tồn tại.");
                    resp.sendRedirect(req.getContextPath() + "/admin/classes");
                    return;
                }
                if (newDate != null && newDate.isBefore(LocalDate.now())) {
                    Flash.error(req, "Không thể lên lịch lại vào ngày trong quá khứ.");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                    return;
                }

                Room r = roomDAO.findById(newRoomId);
                int activeCount = enrollmentDAO.countActiveByClass(classId);
                if (r == null) {
                    Flash.error(req, "Phòng học không tồn tại.");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                    return;
                }
                if (r.getCapacity() < clazz.getCapacity()) {
                    Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho sĩ số tối đa của lớp (" + clazz.getCapacity() + ").");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                    return;
                }
                if (r.getCapacity() < activeCount) {
                    Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho số học viên hiện tại (" + activeCount + ").");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                    return;
                }

                String err = sessionDAO.rescheduleSession(sessionId, newDate, newSlotId, newRoomId);
                if (err == null) Flash.success(req, "Đã đổi lịch buổi học.");
                else Flash.error(req, err);
                resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
                return;
            }

            Flash.error(req, "Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/class-sessions?classId=" + classId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static LocalDate parseDate(String s) {
        String t = trim(s);
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
