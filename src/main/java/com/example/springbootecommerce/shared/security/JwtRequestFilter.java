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
 * Extiende OncePerRequestFilter para garantizar una sola ejecución por request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;
    private final UserDetailsService userDetailsService;

    /**
     * Función principal del filtro que procesa cada request HTTP.
     *
     * @param request Request HTTP
     * @param response Response HTTP
     * @param filterChain Cadena de filtros
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Log del request entrante (solo para debugging en desarrollo)

        if (log.isTraceEnabled()){
            log.trace("Procesando request {} {}", request.getMethod(), request.getRequestURI());
        }

        // Extraer el header de authorization
        final String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);

        // Si no hay header en authorization o no empieza con "Bearer", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith(Constants.BEARER_TOKEN_PREFIX)) {
            log.trace("No se encontró el token JWT en el header de authorization");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer el token de JWT (remover el prefijo "Bearer ")
            final String jwt = authHeader.substring(Constants.BEARER_TOKEN_PREFIX.length()).trim();

            if (jwt.isEmpty()) {
                log.debug("Token JWT vacío en el header");
                filterChain.doFilter(request, response);
                return;
            }

            // Extraer el username del token
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail == null) {
                log.debug("No se pudo extraer el username del token JWT");
                filterChain.doFilter(request, response);
                return;
            }

            // Si el usuario no está authenticate en el contexto de seguridad actual
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                processTokenAuthentication(request,jwt, userEmail);
            }else {
                log.trace("Usuario ya autenticado en el contexto de seguridad");
            }
        }catch (ExpiredJwtException e){
            log.debug("Token JWT expirado para request: {} {}",request.getMethod(),request.getRequestURI());
            handleJwtException(response, "Token expirado", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }catch (UnsupportedJwtException e){
            log.warn("Token JWT no soportado: {}", e.getMessage());
            handleJwtException(response, "Token no soportado", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }catch (MalformedJwtException e){
            log.warn("Token JWT malformado: {}", e.getMessage());
            handleJwtException(response, "Token JWT malformado", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }catch (SecurityException e){
            log.warn("Falla de seguridad en el token JWT: {}", e.getMessage());
            handleJwtException(response, "Token invalido", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }catch (IllegalArgumentException e){
            log.warn("Token JWT invalido: {}", e.getMessage());
            handleJwtException(response, "Token JWT invalido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }catch (Exception e){
            log.error("Error inesperado procesando token JWT: {}", e.getMessage());
            handleJwtException(response, "Error interno procesando el token", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        //Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Procesa la autenticación del token JWT
     *
     * @param request Request HTTP
     * @param jwt Token JWT
     * @param userEmail Email del usuario extraído del token
     */
    private void processTokenAuthentication(HttpServletRequest request,@NonNull String jwt,@NonNull String userEmail){
        try {
            // Cargar los detalles del usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Validar el token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Crear el token de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Establecer detalles adicional al request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la authentication en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuario autenticado exitosamente : {} con roles {}", userEmail, userDetails.getAuthorities());
            }else {
                log.debug("Token JWT invalido para usuario: {} ", userEmail);
            }
        }catch (Exception e){
            log.warn("Error cargando detalles de usuario {}:{}",userEmail,e.getMessage());
        }
    }
    /**
     * Maneja excepciones de JWT enviando respuesta de error apropiada
     *
     * @param response Response HTTP
     * @param message Mensaje de error
     * @param status Código de estado HTTP
     */
    private void handleJwtException(HttpServletResponse response, String message, int status) throws IOException{
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                getErrorCode(status),
                message,
                java.time.LocalDateTime.now()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        log.debug("Enviada respuesta de error JWT: {} - {}", status, message);
    }

    /**
     * Obtiene el código de error basado en el status HTTP
     */
    private String getErrorCode(int status) {
        return switch (status) {
            case HttpServletResponse.SC_UNAUTHORIZED -> "UNAUTHORIZED";
            case HttpServletResponse.SC_BAD_REQUEST -> "BAD_REQUEST";
            case HttpServletResponse.SC_FORBIDDEN -> "FORBIDDEN";
            default -> "JWT_ERROR";
        };
    }

    /**
     * Determina si el filtro debe ser aplicado a este request.
     * Se puede override para excluir ciertos paths de la validación JWT.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // No filtrar endpoints públicos
        boolean isPublicEndpoint =
                path.startsWith("/api/v1/auth/") ||
                        path.startsWith("/api/v1/products") && "GET".equals(method) ||
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/api-docs") ||
                        path.equals("/") ||
                        path.startsWith("/actuator/health");

        if (isPublicEndpoint && log.isDebugEnabled()) {
            log.trace("Saltando la validation de JWT para endpoints público: {} {}",method,path);
        }
        return isPublicEndpoint;
    }

    /**
     * Extrae información adicional del token para logging/auditoría
     */
    private void logTokenInformation(String jwt) {
        if (log.isDebugEnabled()) {
            try {
                Long userId = jwtService.extractUserId(jwt);
                String roles = jwtService.extractRoles(jwt);
                long remainingTime = jwtService.getTokenRemainingTime(jwt);

                log.debug("Token info - UserId: {}, Roles: {}, Remaining time: {}ms",
                        userId, roles, remainingTime);

                // Advertir si el token expira pronto (menos de 5 minutos)
                if (remainingTime > 0 && remainingTime < 300000) { // 5 minutos
                    log.debug("Token expirará pronto en {}ms", remainingTime);
                }

            } catch (Exception e) {
                log.debug("No se pudo extraer información adicional del token: {}", e.getMessage());
            }
        }
    }

    /**
     * Método de utilidad para logging de requests (solo en modo debug)
     */
    private void logRequestDetails(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Request details - Method: {}, URI: {}, Remote Address: {}, User Agent: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"));
        }
    }

    /**
     * Obtiene la dirección IP real del cliente considerando proxies y load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
