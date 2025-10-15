package org.example.service;

import org.example.model.User;
import org.example.security.JwtUtils;
import org.example.security.PasswordUtils;
import org.example.security.ValidationUtils;
import org.example.store.JsonStore;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class AuthService {
    private final JsonStore store;

    public AuthService(JsonStore store) { this.store = store; }

    //registro: valida + hash PBKDF2 + persiste en json
    public void register(String email, char[] password) throws IOException {
        ValidationUtils.validateEmail(email);
        ValidationUtils.validatePasswordPolicy(password);

        var existing = store.findUserByEmail(email);
        if (existing.isPresent()) {
            if (existing.get().isInicioSesionConGoogle()) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese email que inicia sesión con Google. Usá 'Login con Google'.");
            } else {
                throw new IllegalArgumentException("El email ya existe.");
            }
        }

        byte[] salt = PasswordUtils.newSalt();
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = PasswordUtils.hashToBase64(password, salt);

        User u = new User(UUID.randomUUID().toString(), email, hashB64, saltB64, false);
        store.addUser(u);

        //limpiar el array de password en memoria
        java.util.Arrays.fill(password, '\0');
    }


    // login → devuelve jwt (x 30 min)
    public String login(String email, char[] password) {
        User u = store.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));
        if (!u.isActive()) throw new IllegalStateException("Usuario inactivo.");

        if (u.isInicioSesionConGoogle()) {
            throw new IllegalArgumentException("Esta cuenta inicia sesión con Google. Usá la opción 'Login con Google'.");
        }

        boolean ok = PasswordUtils.verify(password, u.getSaltBase64(), u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("Credenciales inválidas.");

        //limpiar password en memoria
        java.util.Arrays.fill(password, '\0');

        return JwtUtils.issueToken(u.getId(), u.getEmail(), 30 * 60);
    }


    public String whoAmI(String token) {
        var jwt = JwtUtils.verify(token);
        return jwt.getClaim("email").asString();
    }

    public String loginWithGoogle(String clientId, String clientSecret) throws Exception {
        var g = new org.example.security.GoogleDeviceLogin(clientId, clientSecret);

        // 1) iniciar device flow
        var step = g.start("openid email profile");
        System.out.println("\n=== Iniciar sesión con Google ===");
        System.out.println("Abrí en el navegador: " + step.verificationUrl);
        if (step.verificationUrlComplete != null) {
            System.out.println("O directamente: " + step.verificationUrlComplete);
        }
        System.out.println("Código: " + step.userCode);
        System.out.println("Esperando autorización...");

        // 2) poll tokens
        var tokens = g.pollForTokens(step.deviceCode, step.interval);

        // 3) validar id_token
        var info = g.validateIdToken(tokens.idToken);
        String aud = info.get("aud").asText();
        if (!aud.equals(clientId)) throw new SecurityException("aud inválido");
        String iss = info.get("iss").asText();
        if (!("https://accounts.google.com".equals(iss) || "accounts.google.com".equals(iss)))
            throw new SecurityException("iss inválido");
        boolean emailVerified = info.has("email_verified") && info.get("email_verified").asText().equals("true");
        String email = info.get("email").asText();
        if (!emailVerified) throw new SecurityException("Email no verificado en Google");

        // 4) upsert usuario en json por email (sin password local)
        var opt = store.findUserByEmail(email);
        org.example.model.User u;
        if (opt.isPresent()) {
            u = opt.get();
        } else {
            u = new org.example.model.User(java.util.UUID.randomUUID().toString(), email, "", "", true);
            store.addUser(u);
        }

        // 5) emitir jwt local (30 min)
        return org.example.security.JwtUtils.issueToken(u.getId(), u.getEmail(), 30 * 60);
    }

}
