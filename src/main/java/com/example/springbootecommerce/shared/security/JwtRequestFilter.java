package com.example.springbootecommerce.shared.security;

import com.example.springbootecommerce.shared.util.Constants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizado para interceptar requests HTTP y validar tokens JWT.
 * Se ejecuta una vez por request y establece el contexto de seguridad si el token es válido.
 *
 * @author Sistema de Seguridad
 * @version 1.2
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Función principal del filtro que procesa cada request HTTP.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("Procesando request {} {}", request.getMethod(), request.getRequestURI());
        }

        final String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(Constants.BEARER_TOKEN_PREFIX)) {
            log.trace("No se encontró token JWT válido en el header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(Constants.BEARER_TOKEN_PREFIX.length()).trim();

            if (jwt.isEmpty()) {
                log.debug("Token JWT vacío");
                filterChain.doFilter(request, response);
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail == null) {
                log.debug("No se pudo extraer username del token");
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                processTokenAuthentication(request, jwt, userEmail);
            }

            if (log.isDebugEnabled()) {
                logTokenInformation(jwt);
            }

        } catch (ExpiredJwtException e) {
            log.debug("Token JWT expirado: {} {}", request.getMethod(), request.getRequestURI());
            handleJwtException(response, "Token expirado", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
            handleJwtException(response, "Token no soportado", HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
            handleJwtException(response, "Token malformado", HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (SecurityException e) {
            log.warn("Error de seguridad en token: {}", e.getMessage());
            handleJwtException(response, "Token inválido", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            handleJwtException(response, "Token JWT inválido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (Exception e) {
            log.error("Error inesperado procesando JWT: {}", e.getMessage(), e);
            handleJwtException(response, "Error interno", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Procesa la autenticación del token JWT.
     */
    private void processTokenAuthentication(HttpServletRequest request,
                                            @NonNull String jwt,
                                            @NonNull String userEmail) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuario autenticado: {} con roles {}", userEmail, userDetails.getAuthorities());
            } else {
                log.debug("Token JWT inválido para: {}", userEmail);
            }
        } catch (Exception e) {
            log.warn("Error cargando detalles de usuario {}: {}", userEmail, e.getMessage());
        }
    }

    /**
     * Maneja excepciones de JWT.
     */
    private void handleJwtException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        String jsonResponse = String.format(
                "{\"success\":false,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                getErrorCode(status), message, java.time.LocalDateTime.now()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private String getErrorCode(int status) {
        return switch (status) {
            case HttpServletResponse.SC_UNAUTHORIZED -> "UNAUTHORIZED";
            case HttpServletResponse.SC_BAD_REQUEST -> "BAD_REQUEST";
            case HttpServletResponse.SC_FORBIDDEN -> "FORBIDDEN";
            default -> "JWT_ERROR";
        };
    }

    /**
     * Determina si el filtro debe saltarse para este request.
     * Lista completa de endpoints públicos incluyendo Swagger/OpenAPI.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Lista de paths públicos que NO requieren JWT
        String[] publicPaths = {
                // Autenticación pública (login, register, etc.)
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh",
                "/api/v1/auth/forgot-password",
                "/api/v1/auth/reset-password",
                "/api/v1/auth/check-email",

                // Documentación API (Swagger/OpenAPI)
                "/swagger-ui",
                "/swagger-ui/",
                "/swagger-ui.html",
                "/swagger-resources",
                "/swagger-resources/",
                "/v3/api-docs",
                "/v3/api-docs/",
                "/api-docs",
                "/api-docs/",
                "/webjars/",

                // Actuator y recursos estáticos
                "/actuator/health",
                "/actuator/health/",
                "/favicon.ico",
                "/error",
                "/css/",
                "/js/",
                "/images/",
                "/static/"
        };

        // Verificar paths públicos específicos
        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                if (log.isTraceEnabled()) {
                    log.trace("Saltando JWT para endpoint público: {} {}", method, path);
                }
                return true;
            }
        }

        // Productos públicos (solo GET)
        if (path.startsWith("/api/v1/products") && "GET".equals(method)) {
            if (log.isTraceEnabled()) {
                log.trace("Saltando JWT para producto público: {} {}", method, path);
            }
            return true;
        }

        // Categorías públicas (solo GET)
        if (path.startsWith("/api/v1/categories") && "GET".equals(method)) {
            if (log.isTraceEnabled()) {
                log.trace("Saltando JWT para categoría pública: {} {}", method, path);
            }
            return true;
        }

        // Reviews públicas (solo GET)
        if (path.startsWith("/api/v1/reviews/product/") && "GET".equals(method)) {
            if (log.isTraceEnabled()) {
                log.trace("Saltando JWT para reviews públicas: {} {}", method, path);
            }
            return true;
        }

        // IMPORTANTE: NO saltar el filtro para endpoints que requieren autenticación
        // como /api/v1/auth/validate-token, /api/v1/auth/change-password, /api/v1/auth/me
        return false;
    }

    /**
     * Log información adicional del token.
     */
    private void logTokenInformation(String jwt) {
        try {
            Long userId = jwtService.extractUserId(jwt);
            String roles = jwtService.extractRoles(jwt);
            long remainingTime = jwtService.getTokenRemainingTime(jwt);

            log.debug("Token - UserId: {}, Roles: {}, Remaining: {}ms", userId, roles, remainingTime);

            if (remainingTime > 0 && remainingTime < 300000) {
                log.warn("Token expira pronto: {}ms para usuario: {}", remainingTime, userId);
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer info adicional del token: {}", e.getMessage());
        }
    }
}