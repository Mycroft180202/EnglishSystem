package Model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class Enrollment implements Serializable {
    private int enrollId;
    private int studentId;
    private int classId;
    private Instant enrolledAt;
    private String status;

    // convenience display fields
    private String studentName;
    private String studentPhone;
    private String classCode;
    private String className;
    private String courseName;
    private LocalDate classStartDate;
    private LocalDate classEndDate;
    private boolean hasSchedule;
    private Integer invoiceId;
    private String invoiceStatus;

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

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Instant enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDate getClassStartDate() {
        return classStartDate;
    }

    public void setClassStartDate(LocalDate classStartDate) {
        this.classStartDate = classStartDate;
    }

    public LocalDate getClassEndDate() {
        return classEndDate;
    }

    public void setClassEndDate(LocalDate classEndDate) {
        this.classEndDate = classEndDate;
    }

    public boolean isHasSchedule() {
        return hasSchedule;
    }

    public void setHasSchedule(boolean hasSchedule) {
        this.hasSchedule = hasSchedule;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }
}
