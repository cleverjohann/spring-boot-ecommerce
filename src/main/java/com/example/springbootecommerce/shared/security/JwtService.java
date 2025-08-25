package com.example.springbootecommerce.shared.security;

import com.example.springbootecommerce.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio para manejo de JSON Web Tokens (JWT).
 * Proporciona funcionalidades para generar, validar y extraer información de tokens JWT.
 * Sigue el principio de responsabilidad única (SRP).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // ========================================================================
    // GENERACIÓN DE TOKENS
    // ========================================================================

    /**
     * Genera un token JWT para el usuario dado
     *
     * @param userDetails Detalles del usuario
     * @return Token JWT como String
     */
    public String generateJwtToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user {}", userDetails.getUsername());

        Map<String,Object> extraClaims = new HashMap<>();

        // Añadir roles como claim personalizados
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        extraClaims.put("roles",roles);

        // Añadir información adicional si el usuario es nuestra entidad personalizada
        if (userDetails instanceof User user) {
            extraClaims.put("userId", user.getId());
            extraClaims.put("fullName",user.getFullName());
        }

        String token = generateToken(extraClaims,userDetails);

        log.info("Token JWT generated for user {}", userDetails.getUsername());
        return token;
    }

    /**
     * Genera un token JWT con claims adicionales
     *
     * @param extraClaims Claims adicionales a incluir
     * @param userDetails Detalles del usuario
     * @return Token JWT como String
     */
    public String generateToken(Map<String,Object> extraClaims,UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Genera un token de refresh (con mayor duración)
     *
     * @param userDetails Detalles del usuario
     * @return Refresh token como String
     */
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("Generating refresh token for user {}", userDetails.getUsername());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (jwtExpiration * 7));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("tokenType", "refresh")
                .signWith(getSignInKey())
                .compact();
    }

    // ========================================================================
    // EXTRACCIÓN DE INFORMACIÓN
    // ========================================================================

    /**
     * Extrae el username (email) del token
     *
     * @param token Token JWT
     * @return Username del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el ID del usuario del token
     *
     * @param token Token JWT
     * @return ID del usuario o null si no está presente
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object userIdClaim = claims.get("userId");

            if (userIdClaim instanceof Integer) {
                return ((Integer) userIdClaim).longValue();
            }else if (userIdClaim instanceof Long){
                return (Long) userIdClaim;
            }
            return null;
        }catch (Exception e){
            log.warn("Error extracting user id from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae los roles del usuario del token
     *
     * @param token Token JWT
     * @return String con roles separados por coma
     */
    public String extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("roles",String.class);
        }catch (Exception e){
            log.warn("Error extracting roles from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae un claim específico del token
     *
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim específico
     * @param <T> Tipo del claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     *
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("Falla de seguridad en token JWT: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vacío o inválido: {}", e.getMessage());
            throw e;
        }
    }

    // ========================================================================
    // VALIDACIÓN DE TOKENS
    // ========================================================================

    /**
     * Válida si un token es válido para un usuario específico
     *
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

            log.debug("Validación de token para usuario {}: {}", username, isValid);
            return isValid;
        }catch (Exception e){
            log.warn("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un token ha expirado
     *
     * @param token Token JWT
     * @return true si el token ha expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean isExpired = expiration.before(new Date());

            if (isExpired) {
                log.debug("Token expirado. Fecha de expiración: {}", expiration);
            }
            return isExpired;
        }catch (Exception e){
            log.warn("Token expirado: {}", e.getMessage());
            return true; // Consideramos el token como expirado si hay error
        }
    }

    /**
     * Verifica si un token es un refresh token
     *
     * @param token Token JWT
     * @return true si es un refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            return "refresh".equals(tokenType);
        }catch (Exception e){
            log.warn("Error validando tipo de token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante antes de que expire el token
     *
     * @param token Token JWT
     * @return Tiempo en milisegundos hasta la expiración, -1 si ya expiró o hay error
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - new Date().getTime();
            return Math.max(remaining, -1);
        }catch (Exception e){
            log.warn("Error calculando tiempo restante del token: {}", e.getMessage());
            return -1;
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================
    /**
     * Obtiene la clave de firma para los tokens
     *
     * @return Clave de firma
     */
    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtiene información de depuración del token (sin datos sensibles)
     *
     * @param token Token JWT
     * @return String con información de depuración
     */
    public String getTokenDebugInfo(String token) {
        try {
            String username = extractUsername(token);
            Date expiration = extractExpiration(token);
            boolean expired = isTokenExpired(token);

            return String.format("Token{user='%s', expires='%s', expired=%s}",
                    username, expiration, expired);
        }catch (Exception e){
            return "Token{invalid or malformed}";
        }
    }

    public Claims parseClaims(String token) {
        return extractAllClaims(token);
    }
}
