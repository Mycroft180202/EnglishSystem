package Util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int DEFAULT_ITERATIONS = 120_000;

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtil() {}

    public static String hashPassword(char[] password) {
        return hashPassword(password, DEFAULT_ITERATIONS);
    }

    public static String hashPassword(char[] password, int iterations) {
        if (password == null || password.length == 0) throw new IllegalArgumentException("Password is empty");
        if (iterations < 10_000) throw new IllegalArgumentException("Iterations too low");

        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, iterations, HASH_BYTES);
        return "pbkdf2_sha256$" + iterations + "$" + b64(salt) + "$" + b64(hash);
    }

    public static boolean verifyPassword(char[] password, String stored) {
        if (password == null || password.length == 0) return false;
        if (stored == null || stored.isBlank()) return false;

        String[] parts = stored.split("\\$");
        if (parts.length != 4) return false;
        if (!"pbkdf2_sha256".equals(parts[0])) return false;

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            return false;
        }

        byte[] salt;
        byte[] expected;
        try {
            salt = b64d(parts[2]);
            expected = b64d(parts[3]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        byte[] actual = pbkdf2(password, salt, iterations, expected.length);
        return constantTimeEquals(actual, expected);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Password hashing unavailable", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private static String b64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] b64d(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}

