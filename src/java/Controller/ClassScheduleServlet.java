package Controller;

import DAO.ClassDAO;
import DAO.ClassScheduleDAO;
import DAO.ClassSessionDAO;
import DAO.EnrollmentDAO;
import DAO.RoomDAO;
import DAO.TeacherDAO;
import DAO.TimeSlotDAO;
import Model.CenterClass;
import Model.ClassSchedule;
import Model.Room;
import Model.Teacher;
import Model.TimeSlot;
import Util.Flash;
import Util.FormToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/admin/class-schedules")
public class ClassScheduleServlet extends HttpServlet {
    private final ClassDAO classDAO = new ClassDAO();
    private final ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private final ClassSessionDAO sessionDAO = new ClassSessionDAO();
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private static final String TOKEN_KEY = "classScheduleToken";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            Flash.consume(req);
            int classId = parseInt(req.getParameter("classId"), -1);
            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null) {
                Flash.error(req, "Vui lòng chọn lớp để xem lịch học.");
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            req.setAttribute("clazz", clazz);
            req.setAttribute("schedules", scheduleDAO.listByClass(classId));
            req.setAttribute("slots", timeSlotDAO.listAll());
            req.setAttribute("teachers", teacherDAO.listActive());
            req.setAttribute("rooms", roomDAO.listActive());
            req.setAttribute("formToken", FormToken.issue(req, TOKEN_KEY));
            req.getRequestDispatcher("/WEB-INF/views/admin/class_schedule.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            int classId = parseInt(req.getParameter("classId"), -1);
            if (classId <= 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/classes");
                return;
            }

            if (!FormToken.consume(req, TOKEN_KEY, req.getParameter("formToken"))) {
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }

            String action = trim(req.getParameter("action"));
            if ("delete".equalsIgnoreCase(action)) {
                int scheduleId = parseInt(req.getParameter("scheduleId"), -1);
                if (scheduleId > 0) {
                    scheduleDAO.delete(scheduleId);
                    Flash.success(req, "Xóa lịch học thành công.");
                }
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }
            if ("update".equalsIgnoreCase(action)) {
                CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
                if (clazz == null || clazz.getTeacherId() == null) {
                    Flash.error(req, "Lớp chưa có giáo viên. Vui lòng gán giáo viên cho lớp trước.");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                    return;
                }

                int scheduleId = parseInt(req.getParameter("scheduleId"), -1);
                ClassSchedule s = new ClassSchedule();
                s.setScheduleId(scheduleId);
                s.setClassId(classId);
                s.setDayOfWeek(parseInt(req.getParameter("dayOfWeek"), 0));
                s.setSlotId(parseInt(req.getParameter("slotId"), 0));
                s.setRoomId(parseInt(req.getParameter("roomId"), 0));
                s.setTeacherId(clazz.getTeacherId());

                String validation = validate(s);
                if (validation != null || scheduleId <= 0) {
                    Flash.error(req, validation == null ? "Lịch học không hợp lệ." : validation);
                    resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                    return;
                }

                Room r = roomDAO.findById(s.getRoomId());
                int activeCount = enrollmentDAO.countActiveByClass(classId);
                if (r == null) {
                    Flash.error(req, "Phòng học không tồn tại.");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                    return;
                }
                if (r.getCapacity() < clazz.getCapacity()) {
                    Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho sĩ số tối đa của lớp (" + clazz.getCapacity() + ").");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                    return;
                }
                if (r.getCapacity() < activeCount) {
                    Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho số học viên hiện tại (" + activeCount + ").");
                    resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                    return;
                }

                try {
                    scheduleDAO.update(s);
                    if ("1".equals(trim(req.getParameter("updateSessions")))) {
                        int deleted = sessionDAO.deleteScheduledFromDate(classId, LocalDate.now());
                        int inserted = sessionDAO.generateFromSchedules(classId, LocalDate.now(), null);
                        Flash.success(req, "Đã cập nhật lịch. Rebuild buổi học: xóa " + deleted + " và tạo " + inserted + " buổi mới.");
                    } else {
                        Flash.success(req, "Đã cập nhật lịch học.");
                    }
                } catch (Exception ex) {
                    Flash.error(req, "Không thể cập nhật lịch (có thể trùng phòng/trùng giáo viên/trùng ca).");
                }

                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }
            if ("generateSessions".equalsIgnoreCase(action)) {
                LocalDate from = parseDate(req.getParameter("fromDate"));
                LocalDate to = parseDate(req.getParameter("toDate"));
                int inserted = sessionDAO.generateFromSchedules(classId, from, to);
                if (inserted > 0) Flash.success(req, "Đã tạo " + inserted + " buổi học từ lịch.");
                else Flash.error(req, "Không có buổi học mới được tạo (có thể chưa có lịch hoặc đã tạo hết).");
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }

            CenterClass clazz = classId > 0 ? classDAO.findById(classId) : null;
            if (clazz == null || clazz.getTeacherId() == null) {
                Flash.error(req, "Lớp chưa có giáo viên. Vui lòng gán giáo viên cho lớp trước.");
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }

            ClassSchedule s = new ClassSchedule();
            s.setClassId(classId);
            s.setDayOfWeek(parseInt(req.getParameter("dayOfWeek"), 0));
            s.setSlotId(parseInt(req.getParameter("slotId"), 0));
            s.setTeacherId(clazz.getTeacherId());
            s.setRoomId(parseInt(req.getParameter("roomId"), 0));

            String validation = validate(s);
            if (validation != null) {
                Flash.error(req, validation);
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }

            Room r = roomDAO.findById(s.getRoomId());
            int activeCount = enrollmentDAO.countActiveByClass(classId);
            if (r == null) {
                Flash.error(req, "Phòng học không tồn tại.");
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }
            if (r.getCapacity() < clazz.getCapacity()) {
                Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho sĩ số tối đa của lớp (" + clazz.getCapacity() + ").");
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }
            if (r.getCapacity() < activeCount) {
                Flash.error(req, "Sức chứa phòng (" + r.getCapacity() + ") không đủ cho số học viên hiện tại (" + activeCount + ").");
                resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
                return;
            }

            try {
                scheduleDAO.create(s);
                Flash.success(req, "Thêm lịch học thành công.");
            } catch (Exception ex) {
                Flash.error(req, "Không thể thêm lịch (có thể trùng phòng/trùng giáo viên/trùng ca).");
            }

            resp.sendRedirect(req.getContextPath() + "/admin/class-schedules?classId=" + classId);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static String validate(ClassSchedule s) {
        if (s.getDayOfWeek() < 1 || s.getDayOfWeek() > 7) return "Thứ không hợp lệ.";
        if (s.getSlotId() <= 0) return "Vui lòng chọn ca học.";
        if (s.getTeacherId() <= 0) return "Vui lòng gán giáo viên cho lớp trước khi tạo/cập nhật lịch.";
        if (s.getRoomId() <= 0) return "Vui lòng chọn phòng học.";
        return null;
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
