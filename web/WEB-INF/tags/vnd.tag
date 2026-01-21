<%@tag description="Format currency as VND" pageEncoding="UTF-8"%>
<%@attribute name="value" required="false" rtexprvalue="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="vi_VN"/>
<c:choose>
    <c:when test="${empty value}">
        <fmt:formatNumber value="0" type="number" groupingUsed="true" maxFractionDigits="0"/>
    </c:when>
    <c:otherwise>
        <fmt:formatNumber value="${value}" type="number" groupingUsed="true" maxFractionDigits="0"/>
    </c:otherwise>
</c:choose>
 VND

