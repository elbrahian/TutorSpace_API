package com.uco.tutorspace_api.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para generar, validar y extraer informacion de tokens JWT.
 *
 * <p>Utiliza el algoritmo HMAC-SHA256 (HS256) con una clave secreta
 * configurada en {@code application.properties} (jwt.secret).
 * La clave debe tener al menos 256 bits (32 caracteres UTF-8).
 *
 * <p>Estructura del token generado:
 * <ul>
 *   <li><b>subject</b>: email del usuario</li>
 *   <li><b>claim "rol"</b>: nombre del rol (ESTUDIANTE, TUTOR, ADMIN)</li>
 *   <li><b>issuedAt</b>: fecha de emision</li>
 *   <li><b>expiration</b>: fecha de expiracion (issuedAt + jwt.expiration ms)</li>
 * </ul>
 */
@Component
public class JwtUtil {

    /** Clave secreta leida desde application.properties (jwt.secret). */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de vida del token en milisegundos (jwt.expiration). Por defecto 86400000 = 24h. */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Construye la clave criptografica HMAC-SHA256 a partir del secreto configurado.
     *
     * @return {@link SecretKey} lista para firmar o verificar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Genera un token JWT firmado para el usuario indicado.
     *
     * @param email email del usuario (se almacena como subject del token)
     * @param rol   nombre del rol del usuario (se almacena como claim "rol")
     * @return token JWT compacto listo para incluir en el header Authorization
     */
    public String generateToken(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrae el email (subject) almacenado en el token.
     *
     * @param token token JWT compacto
     * @return email del usuario propietario del token
     * @throws JwtException si el token es invalido o ha expirado
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol almacenado en el claim "rol" del token.
     *
     * @param token token JWT compacto
     * @return nombre del rol (ESTUDIANTE, TUTOR, ADMIN)
     */
    public String extractRol(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    /**
     * Verifica si el token es valido (firma correcta y no expirado).
     *
     * @param token token JWT compacto
     * @return {@code true} si el token es valido; {@code false} en cualquier otro caso
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token malformado, firma incorrecta o expirado
            return false;
        }
    }

    /**
     * Parsea y valida el token retornando el objeto Claims con todos sus datos.
     *
     * @param token token JWT compacto
     * @return {@link Claims} del token
     * @throws JwtException si la firma es invalida o el token ha expirado
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
