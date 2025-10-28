package org.example.service;

import org.example.model.user.Admin;
import org.example.model.user.RegularUser;
import org.example.model.user.User;
import org.example.security.JwtUtils;
import org.example.security.PasswordUtils;
import org.example.security.ValidationUtils;
import org.example.store.JsonStore;
import org.example.security.RateLimiter;


import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class AuthService {
    private final JsonStore store;
    // 5 intentos cada 10 minutos por email (login con password)
    private static final RateLimiter LOGIN_LIMITER =
            new RateLimiter(5, 10 * 60 * 1000L);

    // 8 intentos cada 10 minutos por email (login con google)
    private static final RateLimiter OAUTH_LIMITER =
            new RateLimiter(8, 10 * 60 * 1000L);

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


        User u = new RegularUser(
                java.util.UUID.randomUUID().toString(), email, hashB64, saltB64);
        u.setInicioSesionConGoogle(false);
        store.addUser(u);


        //limpiar el array de password en memoria
        java.util.Arrays.fill(password, '\0');
    }


    // login → devuelve jwt (x 30 min)
    public String login(String email, char[] password) {
        //aplico rate limiting (strategy for controlling the number of requests a client can make to a service within a specific time period)
        String key = email.toLowerCase();
        if (!LOGIN_LIMITER.allow(key)) {
            long wait = LOGIN_LIMITER.retryAfterSeconds(key);
            throw new IllegalArgumentException("Demasiados intentos. Esperá " + wait + "s e intentá de nuevo.");
        }

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

        return JwtUtils.issueToken(u.getId(), u.getEmail(), u.getRoleName(), 30 * 60);
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

        // 3.1) rate limit por email para OAuth
        String key = ("google:" + email).toLowerCase();
        if (!OAUTH_LIMITER.allow(key)) {
            long wait = OAUTH_LIMITER.retryAfterSeconds(key);
            throw new IllegalArgumentException("Demasiados intentos de OAuth. Esperá " + wait + "s e intentá de nuevo.");
        }

        // 4) upsert usuario en json por email (sin password local)
        var opt = store.findUserByEmail(email);
        User u;
        if (opt.isPresent()) {
            u = opt.get();
            u.setInicioSesionConGoogle(true);
        } else {
            u = new RegularUser(UUID.randomUUID().toString(), email, "", "");
            u.setInicioSesionConGoogle(true);
            store.addUser(u);
        }

        // 5) emitir jwt local (30 min)
        return org.example.security.JwtUtils.issueToken(u.getId(), u.getEmail(),u.getRoleName(), 30 * 60);
    }
    private void requireRoleAtLeast(String token, String required) {
        var jwt = JwtUtils.verify(token);
        String role = jwt.getClaim("role").asString();
        int have = levelOf(role);
        int need = levelOf(required);
        if (have < need) throw new SecurityException("Permiso insuficiente (requiere " + required + ").");
    }
    private int levelOf(String r) {
        if ("ADMIN".equals(r)) return 3;
        if ("MOD".equals(r))   return 2;
        return 1; // USER/null
    }

    public java.util.List<User> listUsers(String token) {
        requireRoleAtLeast(token, "MOD"); // MOD o ADMIN
        return store.findAllUsers();
    }

    public void promoteToModerator(String token, String emailDestino) throws java.io.IOException {
        requireRoleAtLeast(token, "ADMIN");
        var u = store.findUserByEmail(emailDestino)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailDestino));
        var nuevo = new org.example.model.Moderator(u.getId(), u.getEmail(), u.getPasswordHash(), u.getSaltBase64());
        nuevo.setActive(u.isActive());
        nuevo.setInicioSesionConGoogle(u.isInicioSesionConGoogle());
        store.updateUser(nuevo);
    }

    public void promoteToAdmin(String token, String emailDestino) throws java.io.IOException {
        requireRoleAtLeast(token, "ADMIN");
        var u = store.findUserByEmail(emailDestino)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailDestino));
        var nuevo = new Admin(u.getId(), u.getEmail(), u.getPasswordHash(), u.getSaltBase64());
        nuevo.setActive(u.isActive());
        nuevo.setInicioSesionConGoogle(u.isInicioSesionConGoogle());
        store.updateUser(nuevo);
    }


}
