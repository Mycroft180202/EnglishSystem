package Controller;

import DAO.AttendanceDAO;
import DAO.StudentResultDAO;
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

    public static final class GradeRow {
        int enrollId;
        String className;
        String courseName;
        BigDecimal attendancePoint = BigDecimal.TEN;
        BigDecimal test1Point = BigDecimal.ZERO;
        BigDecimal test2Point = BigDecimal.ZERO;
        BigDecimal finalPoint = BigDecimal.ZERO;
        BigDecimal average = BigDecimal.ZERO;
        String rank;
        int totalSessions;
        int attended;
        int absent;
        int excused;

        public int getEnrollId() {
            return enrollId;
        }

        public String getClassName() {
            return className;
        }

        public String getCourseName() {
            return courseName;
        }

        public BigDecimal getAttendancePoint() {
            return attendancePoint;
        }

        public BigDecimal getTest1Point() {
            return test1Point;
        }

        public BigDecimal getTest2Point() {
            return test2Point;
        }

        public BigDecimal getFinalPoint() {
            return finalPoint;
        }

        public BigDecimal getAverage() {
            return average;
        }

        public String getRank() {
            return rank;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public int getAttended() {
            return attended;
        }

        public int getAbsent() {
            return absent;
        }

        public int getExcused() {
            return excused;
        }
    }

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
            Map<Integer, GradeRow> gradeMap = new LinkedHashMap<>();
            for (StudentResultDAO.ResultRow r : rows) {
                GradeRow g = gradeMap.computeIfAbsent(r.getEnrollId(), k -> {
                    GradeRow x = new GradeRow();
                    x.enrollId = k;
                    x.className = r.getClassName();
                    x.courseName = r.getCourseName();
                    return x;
                });

                String type = r.getAssessmentType();
                BigDecimal point = normalizeTo10(r.getScoreValue(), r.getMaxScore());
                if (type == null) continue;
                String t = type.trim().toUpperCase();
                if ((t.equals("TEST1") || t.equals("QUIZ")) && g.test1Point.compareTo(BigDecimal.ZERO) == 0) g.test1Point = point;
                if ((t.equals("TEST2") || t.equals("MIDTERM")) && g.test2Point.compareTo(BigDecimal.ZERO) == 0) g.test2Point = point;
                if (t.equals("FINAL") && g.finalPoint.compareTo(BigDecimal.ZERO) == 0) g.finalPoint = point;
            }

            List<AttendanceDAO.EnrollStats> stats = attendanceDAO.statsByStudent(user.getStudentId());
            Map<Integer, AttendanceDAO.EnrollStats> statMap = new HashMap<>();
            for (AttendanceDAO.EnrollStats s : stats) statMap.put(s.getEnrollId(), s);

            for (GradeRow g : gradeMap.values()) {
                AttendanceDAO.EnrollStats st = statMap.get(g.enrollId);
                if (st != null) {
                    g.totalSessions = st.getTotalSessions();
                    g.attended = st.getAttended();
                    g.absent = st.getAbsent();
                    g.excused = st.getExcused();
                    int missed = g.absent + g.excused;
                    if (g.totalSessions > 0 && missed > (g.totalSessions * 0.2)) g.attendancePoint = BigDecimal.ZERO;
                    else g.attendancePoint = BigDecimal.TEN;
                }

                g.average = g.attendancePoint.multiply(new BigDecimal("0.10"))
                        .add(g.test1Point.multiply(new BigDecimal("0.20")))
                        .add(g.test2Point.multiply(new BigDecimal("0.30")))
                        .add(g.finalPoint.multiply(new BigDecimal("0.40")))
                        .setScale(2, RoundingMode.HALF_UP);

                double avg = g.average.doubleValue();
                if (avg >= 8.0) g.rank = "Xuất sắc";
                else if (avg >= 5.0) g.rank = "Tốt";
                else g.rank = "Khá";
            }

            req.setAttribute("grades", gradeMap.values());
            req.setAttribute("rows", rows); // keep for detail if needed
            req.getRequestDispatcher("/WEB-INF/views/student/results_v2.jsp").forward(req, resp);
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
