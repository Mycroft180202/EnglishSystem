<%@tag description="Format date as dd-MM-yyyy (or custom pattern)" pageEncoding="UTF-8"%>
<%@attribute name="value" required="false" rtexprvalue="true" %>
<%@attribute name="pattern" required="false" rtexprvalue="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@tag import="java.text.SimpleDateFormat" %>
<%@tag import="java.time.LocalDate" %>
<%@tag import="java.time.LocalDateTime" %>
<%@tag import="java.time.OffsetDateTime" %>
<%@tag import="java.time.ZoneId" %>
<%@tag import="java.time.format.DateTimeFormatter" %>
<%@tag import="java.time.format.DateTimeParseException" %>
<%@tag import="java.util.Date" %>

<%
    Object raw = value;
    String pat = (pattern == null || pattern.trim().isEmpty()) ? "dd-MM-yyyy" : pattern.trim();
    String formatted = "";

    if (raw != null) {
        try {
            if (raw instanceof Date) {
                SimpleDateFormat sdf;
                try {
                    sdf = new SimpleDateFormat(pat);
                } catch (IllegalArgumentException ex) {
                    sdf = new SimpleDateFormat("dd-MM-yyyy");
                }
                formatted = sdf.format((Date) raw);
            } else if (raw instanceof LocalDate) {
                DateTimeFormatter formatter;
                try {
                    formatter = DateTimeFormatter.ofPattern(pat);
                } catch (IllegalArgumentException ex) {
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                }
                formatted = ((LocalDate) raw).format(formatter);
            } else if (raw instanceof LocalDateTime) {
                DateTimeFormatter formatter;
                try {
                    formatter = DateTimeFormatter.ofPattern(pat);
                } catch (IllegalArgumentException ex) {
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                }
                formatted = ((LocalDateTime) raw).format(formatter);
            } else if (raw instanceof OffsetDateTime) {
                DateTimeFormatter formatter;
                try {
                    formatter = DateTimeFormatter.ofPattern(pat);
                } catch (IllegalArgumentException ex) {
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                }
                formatted = ((OffsetDateTime) raw).format(formatter);
            } else {
                String s = raw.toString();
                if (s != null) s = s.trim();
                if (s != null && !s.isEmpty()) {
                    DateTimeFormatter formatter;
                    try {
                        formatter = DateTimeFormatter.ofPattern(pat);
                    } catch (IllegalArgumentException ex) {
                        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    }

                    try {
                        formatted = LocalDate.parse(s).format(formatter);
                    } catch (DateTimeParseException ex1) {
                        String t = s.replace('T', ' ').trim();
                        int dot = t.indexOf('.');
                        if (dot >= 0) t = t.substring(0, dot);
                        try {
                            formatted = LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).format(formatter);
                        } catch (DateTimeParseException ex2) {
                            try {
                                formatted = LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).format(formatter);
                            } catch (DateTimeParseException ex3) {
                                try {
                                    formatted = OffsetDateTime.parse(s).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
                                } catch (DateTimeParseException ex4) {
                                    formatted = s;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            formatted = raw.toString();
        }
    }

    jspContext.setAttribute("formatted", formatted);
%>

<c:out value="${formatted}"/>
