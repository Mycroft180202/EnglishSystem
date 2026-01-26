package Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public final class MailUtil {
    private MailUtil() {}

    /**
     * Gmail SMTP sender (STARTTLS).
     *
     * Note: Gmail does NOT use an API key. You need an "App password"
     * (Google Account -> Security -> 2-Step Verification -> App passwords).
     */
    public static void sendEmailVerification(String toEmail, String verifyUrl) {
        Properties p = loadLocalMailConfig();

        String username = firstNonBlank(
                System.getProperty("GMAIL_USERNAME"),
                System.getenv("GMAIL_USERNAME"),
                p.getProperty("GMAIL_USERNAME"),
                DEFAULT_GMAIL_USERNAME
        );
        String appPassword = firstNonBlank(
                System.getProperty("GMAIL_APP_PASSWORD"),
                System.getenv("GMAIL_APP_PASSWORD"),
                p.getProperty("GMAIL_APP_PASSWORD"),
                DEFAULT_GMAIL_APP_PASSWORD
        );
        String from = firstNonBlank(
                System.getProperty("MAIL_FROM"),
                System.getenv("MAIL_FROM"),
                p.getProperty("MAIL_FROM"),
                DEFAULT_MAIL_FROM
        );

        if (appPassword != null) appPassword = appPassword.replace(" ", "");
        if (isBlank(from)) from = username;

        if (isBlank(username) || isBlank(appPassword) || isBlank(from)) {
            System.out.println("[EMAIL_VERIFY] Missing config. to=" + toEmail);
            throw new IllegalStateException(
                    "Missing Gmail SMTP config. Set GMAIL_USERNAME, GMAIL_APP_PASSWORD and MAIL_FROM (sysprop/env or local file)."
            );
        }

        String subject = "Xác thực email - EnglishSystem";
        String body = ""
                + "Chào bạn,\r\n\r\n"
                + "Vui lòng bấm link để xác thực email:\r\n"
                + verifyUrl + "\r\n\r\n"
                + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\r\n";

        try {
            GmailSmtpSender.sendText(username, appPassword, from, toEmail, subject, body);
            System.out.println("[EMAIL_VERIFY] SENT to=" + toEmail);
        } catch (IOException ex) {
            throw new RuntimeException("Send email failed: " + ex.getMessage(), ex);
        }
    }

    private static final String DEFAULT_GMAIL_USERNAME = "blabla180202@gmail.com";
    private static final String DEFAULT_GMAIL_APP_PASSWORD = "hhzq axlv btho tvrb";
    private static final String DEFAULT_MAIL_FROM = "blabla180202@gmail.com";

    private static Properties loadLocalMailConfig() {
        Properties props = new Properties();
        String customPath = firstNonBlank(System.getProperty("MAIL_CONFIG_PATH"), System.getenv("MAIL_CONFIG_PATH"));
        Path path = !isBlank(customPath)
                ? Paths.get(customPath.trim())
                : Paths.get(System.getProperty("user.home"), ".englishsystem-mail.properties");

        if (!Files.exists(path)) return props;
        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (Exception ignored) {
        }
        return props;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    /**
     * Minimal SMTP client for Gmail (STARTTLS on 587).
     * Avoids adding extra dependencies (jakarta.mail) to this NetBeans Ant project.
     */
    private static final class GmailSmtpSender {
        private static final String HOST = "smtp.gmail.com";
        private static final int PORT = 587;

        private GmailSmtpSender() {}

        static void sendText(String username, String appPassword, String from, String to,
                             String subject, String body) throws IOException {
            try (Socket plain = new Socket(HOST, PORT)) {
                plain.setSoTimeout(20000);

                BufferedReader in = new BufferedReader(new InputStreamReader(plain.getInputStream(), StandardCharsets.US_ASCII));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(plain.getOutputStream(), StandardCharsets.US_ASCII));

                expect(in, 220);
                sendLine(out, "EHLO localhost");
                expect(in, 250);

                sendLine(out, "STARTTLS");
                expect(in, 220);

                SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
                try (SSLSocket tls = (SSLSocket) f.createSocket(plain, HOST, PORT, true)) {
                    tls.setUseClientMode(true);
                    tls.startHandshake();

                    BufferedReader tlsIn = new BufferedReader(new InputStreamReader(tls.getInputStream(), StandardCharsets.US_ASCII));
                    BufferedWriter tlsOut = new BufferedWriter(new OutputStreamWriter(tls.getOutputStream(), StandardCharsets.US_ASCII));

                    sendLine(tlsOut, "EHLO localhost");
                    expect(tlsIn, 250);

                    // AUTH LOGIN
                    sendLine(tlsOut, "AUTH LOGIN");
                    expect(tlsIn, 334);
                    sendLine(tlsOut, base64(username));
                    expect(tlsIn, 334);
                    sendLine(tlsOut, base64(appPassword));
                    expect(tlsIn, 235);

                    sendLine(tlsOut, "MAIL FROM:<" + from + ">");
                    expect(tlsIn, 250);
                    sendLine(tlsOut, "RCPT TO:<" + to + ">");
                    expect(tlsIn, 250);
                    sendLine(tlsOut, "DATA");
                    expect(tlsIn, 354);

                    // Headers + body
                    writeData(tlsOut, "From: " + from);
                    writeData(tlsOut, "To: " + to);
                    writeData(tlsOut, "Subject: " + subject);
                    writeData(tlsOut, "MIME-Version: 1.0");
                    writeData(tlsOut, "Content-Type: text/plain; charset=UTF-8");
                    writeData(tlsOut, "");
                    for (String line : body.replace("\r\n", "\n").split("\n", -1)) {
                        // Dot-stuffing
                        if (line.startsWith(".")) line = "." + line;
                        writeData(tlsOut, line);
                    }
                    writeData(tlsOut, ".");
                    tlsOut.flush();
                    expect(tlsIn, 250);

                    sendLine(tlsOut, "QUIT");
                }
            }
        }

        private static void sendLine(BufferedWriter out, String line) throws IOException {
            out.write(line);
            out.write("\r\n");
            out.flush();
        }

        private static void writeData(BufferedWriter out, String line) throws IOException {
            out.write(line);
            out.write("\r\n");
        }

        private static String base64(String s) {
            return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }

        private static void expect(BufferedReader in, int expectedCode) throws IOException {
            int code = readReplyCode(in);
            if (code != expectedCode && !(expectedCode == 250 && code == 251)) {
                throw new IOException("SMTP error. expected=" + expectedCode + " got=" + code);
            }
        }

        private static int readReplyCode(BufferedReader in) throws IOException {
            String line = in.readLine();
            if (line == null || line.length() < 3) throw new IOException("SMTP: empty response");
            String codeStr = line.substring(0, 3);
            int code;
            try {
                code = Integer.parseInt(codeStr);
            } catch (NumberFormatException ex) {
                throw new IOException("SMTP: invalid response: " + line);
            }

            // Multi-line replies: "250-" ... "250 "
            if (line.length() >= 4 && line.charAt(3) == '-') {
                String prefix = codeStr + " ";
                while (true) {
                    String next = in.readLine();
                    if (next == null) throw new IOException("SMTP: unexpected EOF");
                    if (next.startsWith(prefix)) break;
                }
            }
            return code;
        }
    }
}
