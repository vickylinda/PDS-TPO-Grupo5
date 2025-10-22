package org.example.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.Date;

public final class JwtUtils {
    private JwtUtils() {}

    private static final String ISSUER = "console-auth";

    private static final String SECRET = System.getenv()
            .getOrDefault("APP_JWT_SECRET", "DEV_ONLY_change_me_please_1234567890");
    private static final Algorithm ALG = Algorithm.HMAC256(SECRET);

    // ===== emitir SIN rol =====
    public static String issueToken(String userId, String email, long ttlSeconds) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("uid", userId)
                .withClaim("email", email)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(ALG);
    }

    // ===== emitir CON rol =====
    public static String issueToken(String userId, String email, String role, int expiresInSeconds) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("uid", userId)
                .withClaim("email", email)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(expiresInSeconds)))
                .sign(ALG);
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(ALG)
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }
}
