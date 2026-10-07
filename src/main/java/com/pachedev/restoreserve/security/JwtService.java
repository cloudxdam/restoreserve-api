package com.pachedev.restoreserve.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio que se encarga de :
 * - generar el token cuando un usuario se loguea
 * - extra información del token (como el username)
 * - validar que el token sea legítimo y si está vigente
 */

@Service
public class JwtService {

    /**
     * Inyecta el valor de la clave secreta definida en application.properties
     */
    @Value("${jwt.secret-key}")
    private String secretKey;

    /**
     * Inyecta el tiempo de vida del token que hemos definido en milisegundos en
     * application.properties
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Crea un nuevo token con el nombre del usuario, la fecha de emisión y la fecha
     * de caducidad, firmándolo con la firma digital
     */
    public String generateToken(String username) {

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Convierte la clave del application.properties en una firma digital válida
     * para el algoritmo.
     */
    private SecretKey getSigningKey() {
        // Decodifica la cadena de texto que hemos definido en application.properties
        byte[] Keybytes = Decoders.BASE64.decode(secretKey);

        // Genera la firma usando el algoritmo HMAC-SHA
        return Keys.hmacShaKeyFor(Keybytes);
    }

    /**
     * Abre el token utilizando la firma digital y extrae su contenido (claims).
     */
    private Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Extrae el nombre de usuario que hay en el token.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Verifica que la fecha de expiración del token no sea anterior a la actual.
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = extractClaims(token).getExpiration();

        return expirationDate.before(new Date());
    }

    /**
     * Verifica que el token pertenece al usuario que lo envía y que no ha expirado
     * su fecha de validez.
     */
    public boolean isTokenValid(String token, String username) {
        String tokenUsername = extractUsername(token);

        return tokenUsername.equals(username) && !isTokenExpired(token);

    }
}
