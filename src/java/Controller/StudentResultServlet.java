package Controller;

import DAO.AttendanceDAO;
import DAO.StudentResultDAO;
import Model.StudentGradeRow;
import Model.User;
import Util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/student/results")
public class StudentResultServlet extends HttpServlet {
    private final StudentResultDAO resultDAO = new StudentResultDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

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

            List<StudentResultDAO.ResultRow> rows = resultDAO.listResultsByStudent(user.getStudentId());
            Map<Integer, StudentGradeRow> gradeMap = new LinkedHashMap<>();
            for (StudentResultDAO.ResultRow r : rows) {
                StudentGradeRow g = gradeMap.computeIfAbsent(r.getEnrollId(), k -> {
                    StudentGradeRow x = new StudentGradeRow();
                    x.setEnrollId(k);
                    x.setClassName(r.getClassName());
                    x.setCourseName(r.getCourseName());
                    return x;
                });

                String type = r.getAssessmentType();
                BigDecimal point = normalizeTo10(r.getScoreValue(), r.getMaxScore());
                if (type == null) continue;
                String t = type.trim().toUpperCase();
                if ((t.equals("TEST1") || t.equals("QUIZ")) && g.getTest1Point().compareTo(BigDecimal.ZERO) == 0) g.setTest1Point(point);
                if ((t.equals("TEST2") || t.equals("MIDTERM")) && g.getTest2Point().compareTo(BigDecimal.ZERO) == 0) g.setTest2Point(point);
                if (t.equals("FINAL") && g.getFinalPoint().compareTo(BigDecimal.ZERO) == 0) g.setFinalPoint(point);
            }

            List<AttendanceDAO.EnrollStats> stats = attendanceDAO.statsByStudent(user.getStudentId());
            Map<Integer, AttendanceDAO.EnrollStats> statMap = new HashMap<>();
            for (AttendanceDAO.EnrollStats s : stats) statMap.put(s.getEnrollId(), s);

            for (StudentGradeRow g : gradeMap.values()) {
                AttendanceDAO.EnrollStats st = statMap.get(g.getEnrollId());
                if (st != null) {
                    g.setTotalSessions(st.getTotalSessions());
                    g.setAttended(st.getAttended());
                    g.setAbsent(st.getAbsent());
                    g.setExcused(st.getExcused());
                    int missed = g.getAbsent() + g.getExcused();
                    if (g.getTotalSessions() > 0 && missed > (g.getTotalSessions() * 0.2)) g.setAttendancePoint(BigDecimal.ZERO);
                    else g.setAttendancePoint(BigDecimal.TEN);
                }

                g.setAverage(g.getAttendancePoint().multiply(new BigDecimal("0.10"))
                        .add(g.getTest1Point().multiply(new BigDecimal("0.20")))
                        .add(g.getTest2Point().multiply(new BigDecimal("0.30")))
                        .add(g.getFinalPoint().multiply(new BigDecimal("0.40")))
                        .setScale(2, RoundingMode.HALF_UP));

                double avg = g.getAverage().doubleValue();
                if (avg >= 8.0) g.setRank("Xuất sắc");
                else if (avg >= 5.0) g.setRank("Tốt");
                else g.setRank("Khá");
            }

            req.setAttribute("grades", gradeMap.values());
            req.setAttribute("rows", rows); // keep for detail if needed
            req.getRequestDispatcher("/WEB-INF/views/student/results_v3.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private static BigDecimal normalizeTo10(BigDecimal score, BigDecimal max) {
        if (score == null || max == null) return BigDecimal.ZERO;
        if (max.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return score.multiply(BigDecimal.TEN).divide(max, 2, RoundingMode.HALF_UP);
    }
}
