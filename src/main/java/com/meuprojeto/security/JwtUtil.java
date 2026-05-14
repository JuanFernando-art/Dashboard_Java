package com.meuprojeto.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.meuprojeto.config.AppConfig;
import com.meuprojeto.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JwtUtil {
    private static final String ISSUER = "GestorERP";
    private static final long EXPIRATION_HOURS = 2;
    private static final String DEV_JWT_SECRET = "dev-local-JWT_SECRET-change-before-production";

    private JwtUtil() {}

    public static String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(String.valueOf(usuario.getIdUsuario()))
                .withClaim("nome", usuario.getNome())
                .withClaim("email", usuario.getEmail())
                .withIssuedAt(Date.from(agora))
                .withExpiresAt(Date.from(agora.plus(EXPIRATION_HOURS, ChronoUnit.HOURS)))
                .sign(getAlgorithm());
    }

    public static int validarToken(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm())
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return Integer.parseInt(jwt.getSubject());
    }

    private static Algorithm getAlgorithm() {
        return Algorithm.HMAC256(getSecretBytes());
    }

    private static byte[] getSecretBytes() {
        String secret = AppConfig.envOrDevFallback("JWT_SECRET", DEV_JWT_SECRET);

        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel carregar a chave JWT.", e);
        }
    }
}
