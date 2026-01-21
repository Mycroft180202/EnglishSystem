package Model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Course implements Serializable {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String description;
    private String level;
    private int durationWeeks;
    private BigDecimal standardFee;
    private String status;

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public BigDecimal getStandardFee() {
        return standardFee;
    }

    public void setStandardFee(BigDecimal standardFee) {
        this.standardFee = standardFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

