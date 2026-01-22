package Model;

import java.io.Serializable;

public class AttendanceRow implements Serializable {
    private int enrollId;
    private int studentId;
    private String studentName;
    private String studentPhone;
    private String status; // ATTENDED/ABSENT/EXCUSED
    private String note;

    // stats (up to current session date)
    private int attendedCount;
    private int absentCount;
    private int excusedCount;
    private int totalSessionsToDate;

    // absence request (optional)
    private String requestStatus;
    private String requestReason;
    private String requestCreatedAt;

    public int getEnrollId() {
        return enrollId;
    }

    public void setEnrollId(int enrollId) {
        this.enrollId = enrollId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getAttendedCount() {
        return attendedCount;
    }

    public void setAttendedCount(int attendedCount) {
        this.attendedCount = attendedCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getExcusedCount() {
        return excusedCount;
    }

    public void setExcusedCount(int excusedCount) {
        this.excusedCount = excusedCount;
    }

    public int getTotalSessionsToDate() {
        return totalSessionsToDate;
    }

    public void setTotalSessionsToDate(int totalSessionsToDate) {
        this.totalSessionsToDate = totalSessionsToDate;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public String getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(String requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }
}
