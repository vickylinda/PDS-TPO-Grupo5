package org.example.security;
import java.util.regex.Pattern;
public final class ValidationUtils {
    private ValidationUtils() {}

    private static final Pattern EMAIL_RE =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_RE.matcher(email).matches())
            throw new IllegalArgumentException("Email inválido.");
    }

    // >=8 caracteres, al menos 1 letra y 1 número
    public static void validatePasswordPolicy(char[] password) {
        if (password == null || password.length < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        boolean hasLetter = false, hasDigit = false;
        for (char c : password) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c))  hasDigit = true;
        }
        if (!hasLetter || !hasDigit)
            throw new IllegalArgumentException("La contraseña debe incluir letras y números.");
    }
}
