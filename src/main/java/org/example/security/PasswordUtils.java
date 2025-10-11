package org.example.security;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
public final class PasswordUtils {
    private PasswordUtils() {}

    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;       // 256 bits
    private static final int ITERATIONS = 120_000;

    public static byte[] newSalt() {
        byte[] s = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(s);
        return s;
    }

    public static String hashToBase64(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BYTES * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hash PBKDF2", e);
        }
    }

    public static boolean verify(char[] password, String saltBase64, String expectedHashBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        String h = hashToBase64(password, salt);
        return constantTimeEquals(h, expectedHashBase64);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }
}
