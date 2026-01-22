package Model;

import java.math.BigDecimal;

public class StudentGradeRow {
    private int enrollId;
    private String className;
    private String courseName;
    private BigDecimal attendancePoint = BigDecimal.TEN;
    private BigDecimal test1Point = BigDecimal.ZERO;
    private BigDecimal test2Point = BigDecimal.ZERO;
    private BigDecimal finalPoint = BigDecimal.ZERO;
    private BigDecimal average = BigDecimal.ZERO;
    private String rank;
    private int totalSessions;
    private int attended;
    private int absent;
    private int excused;

    public int getEnrollId() { return enrollId; }
    public void setEnrollId(int enrollId) { this.enrollId = enrollId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public BigDecimal getAttendancePoint() { return attendancePoint; }
    public void setAttendancePoint(BigDecimal attendancePoint) { this.attendancePoint = attendancePoint; }

    public BigDecimal getTest1Point() { return test1Point; }
    public void setTest1Point(BigDecimal test1Point) { this.test1Point = test1Point; }

    public BigDecimal getTest2Point() { return test2Point; }
    public void setTest2Point(BigDecimal test2Point) { this.test2Point = test2Point; }

    public BigDecimal getFinalPoint() { return finalPoint; }
    public void setFinalPoint(BigDecimal finalPoint) { this.finalPoint = finalPoint; }

    public BigDecimal getAverage() { return average; }
    public void setAverage(BigDecimal average) { this.average = average; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getAttended() { return attended; }
    public void setAttended(int attended) { this.attended = attended; }

    public int getAbsent() { return absent; }
    public void setAbsent(int absent) { this.absent = absent; }

    public int getExcused() { return excused; }
    public void setExcused(int excused) { this.excused = excused; }
}

