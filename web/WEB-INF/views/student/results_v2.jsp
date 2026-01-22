<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Kết quả học tập" active="student-results">
    <div class="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-3">
        <div>
            <h3 class="m-0">Kết quả học tập</h3>
            <div class="text-muted small">
                Điểm trung bình = Chuyên cần 10% + Test 1 20% + Test 2 30% + Final 40%.
                Nghỉ quá 20% số buổi → điểm chuyên cần = 0. Nghỉ có phép tối đa 3 buổi.
            </div>
        </div>
    </div>

    <div class="table-responsive mb-4">
        <table class="table table-striped table-hover align-middle">
            <thead>
                <tr>
                    <th>Lớp</th>
                    <th>Khóa</th>
                    <th>Chuyên cần</th>
                    <th>Test 1</th>
                    <th>Test 2</th>
                    <th>Final</th>
                    <th>TB</th>
                    <th>Xếp loại</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${grades}" var="g">
                    <tr>
                        <td><c:out value="${g.className}"/></td>
                        <td><c:out value="${g.courseName}"/></td>
                        <td>
                            <div class="fw-semibold"><c:out value="${g.attendancePoint}"/></div>
                            <div class="small text-muted">
                                <c:out value="${g.attended}"/>/<c:out value="${g.totalSessions}"/> attended
                                · <c:out value="${g.absent}"/> absent
                                · <c:out value="${g.excused}"/> excused
                            </div>
                        </td>
                        <td><c:out value="${g.test1Point}"/></td>
                        <td><c:out value="${g.test2Point}"/></td>
                        <td><c:out value="${g.finalPoint}"/></td>
                        <td class="fw-semibold"><c:out value="${g.average}"/></td>
                        <td>
                            <span class="badge ${g.rank == 'Xuất sắc' ? 'text-bg-success' : (g.rank == 'Tốt' ? 'text-bg-primary' : 'text-bg-secondary')}">
                                <c:out value="${g.rank}"/>
                            </span>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty grades}">
                    <tr>
                        <td colspan="8" class="text-center text-muted">Chưa có dữ liệu.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <details>
        <summary class="text-muted">Xem chi tiết đầu điểm</summary>
        <div class="table-responsive mt-2">
            <table class="table table-striped table-hover align-middle">
                <thead>
                    <tr>
                        <th>Lớp</th>
                        <th>Khóa</th>
                        <th>Đầu điểm</th>
                        <th>Type</th>
                        <th>Điểm</th>
                        <th>Max</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${rows}" var="r">
                        <tr>
                            <td><c:out value="${r.className}"/></td>
                            <td><c:out value="${r.courseName}"/></td>
                            <td><c:out value="${r.assessmentName}"/></td>
                            <td><c:out value="${r.assessmentType}"/></td>
                            <td><c:out value="${r.scoreValue}"/></td>
                            <td><c:out value="${r.maxScore}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr>
                            <td colspan="6" class="text-center text-muted">Chưa có dữ liệu điểm.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </details>
</t:layout>

