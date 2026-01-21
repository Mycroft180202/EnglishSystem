package Util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CodeUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CodeUtil() {}

    public static String invoiceCode() {
        int r = RNG.nextInt(1_000_000);
        return "INV" + LocalDateTime.now().format(TS) + String.format("%06d", r);
    }
}

