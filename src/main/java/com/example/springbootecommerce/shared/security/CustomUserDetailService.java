package com.example.springbootecommerce.shared.security;

import com.example.springbootecommerce.user.entity.User;
import com.example.springbootecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación personalizada de UserDetailsService para Spring Security.
 * Se encarga de cargar los detalles del usuario desde la base de datos.
 * Sigue el principio de responsabilidad única (SRP).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Carga los detalles del usuario por email (username).
     * Esta función es llamada por Spring Security durante el proceso de autenticación.
     *
     * @param email El email del usuario (usado como username)
     * @return UserDetails con la información del usuario y sus roles
     * @throws UsernameNotFoundException si el usuario no existe o está inactivo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Intento de authentication con email nulo o vacío");
            throw new UsernameNotFoundException("Email no puede ser nulo o vacío");
        }

        // Buscar el usuario activo por email
        User user = userRepository.findByEmailAndIsActiveTrue(email.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado o inactivo: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado o inactivo: " + email);
                });

        log.debug("Usuario encontrado: {} con roles {}", user ,user.getRoles().size());

        // Verificar que el usuario tenga al menos un rol
        if (user.getRoles().isEmpty()) {
            log.warn("Usuario sin roles asignados: {}", email);
            throw new UsernameNotFoundException("Usuario no encontrado o inactivo: " + email);
        }

        log.info("Authentication exitosa para el usuario: {}", user.getEmail());

        // La entidad User implementa UserDetails, por lo que se puede retornar directamente
        return user;
    }

    /**
     * Función auxiliar para verificar si un usuario existe y está activo.
     * Útil para validaciones sin cargar todos los detalles.
     *
     * @param email Email del usuario
     * @return true si el usuario existe y está activo
     */
    public boolean userExistsAndIsActive(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        boolean exists = userRepository.existsByEmailAndIsActiveTrue(email.trim().toLowerCase());
        log.debug("Verificación de existencia para {}: {}", email, exists);

        return exists;
    }

    /**
     * Función auxiliar para obtener información básica del usuario sin cargar relaciones.
     * Útil para operaciones que no requieren roles o direcciones.
     *
     * @param email Email del usuario
     * @return User entity o null si no existe
     */
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userRepository.findByEmailAndIsActiveTrue(email.trim().toLowerCase())
                .orElse(null);
    }

    /**
     * Invalida la caché de usuario (si se implementa caché en el futuro).
     * Útil cuando se actualizan roles o se desactiva un usuario.
     *
     * @param email Email del usuario
     */
    public void evictUserFromCache(String email) {
        log.debug("Invalidando caché para usuario: {}", email);
        //TODO: Implementar lógica de invalidación de caché si se añade caché en el futuro
        // Implementación futura si se añade caché
        // Por ahora es un placeholder para futuras mejoras
    }

    /**
     * Refresca los roles del usuario en el contexto de seguridad actual.
     * Útil después de cambios de roles sin requerir nuevo login.
     *
     * @param email Email del usuario
     * @return UserDetails actualizado o null si no existe
     */
    public UserDetails refreshUserDetails(String email) {
        try {
            return loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            log.warn("No se pudo refrescar los detalles del usuario: {}", email);
            return null;
        }
    }

    /**
     * Válida las credenciales del usuario de manera explícita.
     * Útil para validaciones adicionales de seguridad.
     *
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return true si las credenciales son válidas
     */
    public boolean validateUserCredentials(String email, String password) {
        if (email == null || password == null) {
            return false;
        }

        try {
            User user = (User) loadUserByUsername(email);
            // Nota: En una implementación real, aquí se compararía con PasswordEncoder
            // Por ahora solo verificamos que el usuario existe
            return user != null && user.isEnabled() &&
                    passwordEncoder.matches(password,user.getPassword());
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Obtiene estadísticas de autenticación para monitoreo.
     *
     * @return Información básica sobre usuarios activos
     */
    public AuthenticationStatistics getAuthenticationStatistics() {
        long totalActiveUsers = userRepository.countByIsActiveTrue();

        // En el futuro se pueden añadir más estadísticas:
        // - Usuarios conectados actualmente
        // - Intentos de login fallidos
        // - etc.

        return new AuthenticationStatistics(totalActiveUsers, 0L, 0L);
    }

    /**
     * Record para estadísticas de autenticación
     */
    public record AuthenticationStatistics(
            long totalActiveUsers,
            long currentlyLoggedIn,
            long failedLoginAttempts
    ) {}

}
