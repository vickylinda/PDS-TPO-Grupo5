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

        if (store.findUserByEmail(email).isPresent())
            throw new IllegalArgumentException("El email ya existe.");

        byte[] salt = PasswordUtils.newSalt();
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = PasswordUtils.hashToBase64(password, salt);

        User u = new User(UUID.randomUUID().toString(), email, hashB64, saltB64);
        store.addUser(u);
    }

    // login → devuelve jwt (x 30 min)
    public String login(String email, char[] password) {
        User u = store.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));
        if (!u.isActive()) throw new IllegalStateException("Usuario inactivo.");

        boolean ok = PasswordUtils.verify(password, u.getSaltBase64(), u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("Credenciales inválidas.");

        return JwtUtils.issueToken(u.getId(), u.getEmail(), 30 * 60);
    }

    public String whoAmI(String token) {
        var jwt = JwtUtils.verify(token);
        return jwt.getClaim("email").asString();
    }
}
