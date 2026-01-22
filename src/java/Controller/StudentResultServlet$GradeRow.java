package Controller;

/**
 * Compatibility shim.
 *
 * Some NetBeans/Tomcat in-place deploys previously missed copying the
 * compiler-generated inner class {@code StudentResultServlet$GradeRow.class},
 * which caused Tomcat annotation scanning to fail at startup.
 *
 * This top-level class ensures {@code Controller.StudentResultServlet$GradeRow}
 * always exists so the webapp can start even if an old compiled
 * {@code StudentResultServlet.class} is still present during incremental deploys.
 */
public class StudentResultServlet$GradeRow {
}

