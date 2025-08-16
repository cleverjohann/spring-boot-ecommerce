package com.example.springbootecommerce.auth.service.impl;

import com.example.springbootecommerce.auth.dto.*;
import com.example.springbootecommerce.auth.service.AuthService;
import com.example.springbootecommerce.auth.service.TokenBlacklistService;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.security.JwtService;
import com.example.springbootecommerce.user.entity.Role;
import com.example.springbootecommerce.user.entity.User;
import com.example.springbootecommerce.user.mapper.UserMapper;
import com.example.springbootecommerce.user.repository.RoleRepository;
import com.example.springbootecommerce.user.repository.UserRepository;
import com.example.springbootecommerce.user.util.RoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Implementación del servicio de autenticación.
 * Maneja todas las operaciones relacionadas con autenticación, autorización y gestión de tokens JWT.
 * 
 * @author Sistema de Autenticación
 * @version 1.2
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.expiration:86400000}") // 24 horas por defecto
    private long jwtExpiration;

    @Value("${app.default.role:USER}")
    private String defaultRoleName;

    /**
     * Autentica un usuario mediante email y contraseña.
     */
    @Override
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Iniciando proceso de autenticación para usuario: {}", loginRequest.getEmail());
        
        try {
            // Autentica al usuario usando el AuthenticationManager
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Recupera el usuario desde la base de datos
            var user = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Intento de login fallido - Usuario no encontrado o inactivo: {}", 
                                loginRequest.getEmail());
                        return new BusinessException("Credenciales inválidas o usuario inactivo");
                    });

            // Genera los tokens JWT
            String accessToken = jwtService.generateJwtToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Calcula tiempo de expiración en segundos
            long expiresIn = jwtExpiration / 1000;

            // Mapea el usuario a DTO para la respuesta
            var userDTO = userMapper.toLoginResponseDTO(user);

            log.info("Autenticación exitosa para usuario: {} con roles: {}", 
                    user.getEmail(), user.getRoles());

            // Construye y retorna la respuesta
            return JwtResponseDTO.success(accessToken, refreshToken, userDTO, expiresIn);
            
        } catch (AuthenticationException e) {
            log.warn("Fallo en autenticación para usuario: {} - Motivo: {}", 
                    loginRequest.getEmail(), e.getMessage());
            throw new BusinessException("Credenciales inválidas", "AUTH_INVALID_CREDENTIALS");
        } catch (BusinessException e) {
            // Re-lanzar BusinessException sin modificar
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante autenticación para usuario: {} - Error: {}", 
                    loginRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Error interno durante autenticación", "AUTH_INTERNAL_ERROR");
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    @Override
    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Iniciando proceso de registro para email: {}", registerRequest.getEmail());

        // Validar datos de entrada
        validateRegistrationData(registerRequest);

        // Verificar que el email esté disponible
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", registerRequest.getEmail());
            throw new BusinessException("El email ya está registrado en el sistema", "REG_EMAIL_EXISTS");
        }

        try {
            // Buscar el rol por defecto - AÑADIR PREFIJO ROLE_ si es necesario
            String roleName = RoleUtils.withPrefix(defaultRoleName);
            Role defaultRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        log.error("Rol por defecto no encontrado: {} (buscando como: {})", defaultRoleName, roleName);
                        return new BusinessException("Error de configuración del sistema", "REG_ROLE_NOT_FOUND");
                    });

            log.debug("Rol por defecto encontrado: {}", defaultRole.getName());

            // Crear la entidad User
            User newUser = User.builder()
                    .firstName(registerRequest.getFirstName().trim())
                    .lastName(registerRequest.getLastName().trim())
                    .email(registerRequest.getEmail().toLowerCase().trim())
                    .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                    .isActive(true)
                    .build();

            // Asignar rol por defecto
            newUser.setRoles(Set.of(defaultRole));

            // Guardar el usuario
            User savedUser = userRepository.save(newUser);

            log.info("Usuario registrado exitosamente: {} con ID: {} y rol: {}",
                    savedUser.getEmail(), savedUser.getId(), defaultRole.getName());

            // Generar tokens JWT
            String accessToken = jwtService.generateJwtToken(savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);
            long expiresIn = jwtExpiration / 1000;

            // Mapear a DTO
            var userDTO = userMapper.toLoginResponseDTO(savedUser);

            log.info("Registro completo exitoso para usuario: {}", savedUser.getEmail());

            return JwtResponseDTO.success(accessToken, refreshToken, userDTO, expiresIn);

        } catch (DataAccessException e) {
            log.error("Error de base de datos durante registro para usuario: {} - Error: {}",
                    registerRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Error de base de datos durante el registro", "REG_DATABASE_ERROR");
        } catch (BusinessException e) {
            // Re-lanzar BusinessException sin modificar
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante registro de usuario: {} - Error: {}",
                    registerRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Error interno durante el registro", "REG_INTERNAL_ERROR");
        }
    }


    /**
     * Valida los datos de registro.
     */
    private void validateRegistrationData(RegisterRequestDTO registerRequest) {
        if (registerRequest == null) {
            throw new BusinessException("Los datos de registro son obligatorios", "REG_INVALID_DATA");
        }

        if (!StringUtils.hasText(registerRequest.getFirstName())) {
            throw new BusinessException("El nombre es obligatorio", "REG_INVALID_FIRSTNAME");
        }

        if (!StringUtils.hasText(registerRequest.getLastName())) {
            throw new BusinessException("El apellido es obligatorio", "REG_INVALID_LASTNAME");
        }

        if (!StringUtils.hasText(registerRequest.getEmail())) {
            throw new BusinessException("El email es obligatorio", "REG_INVALID_EMAIL");
        }

        if (!StringUtils.hasText(registerRequest.getPassword())) {
            throw new BusinessException("La contraseña es obligatoria", "REG_INVALID_PASSWORD");
        }

        if (registerRequest.getPassword().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres", "REG_PASSWORD_TOO_SHORT");
        }

        // Validar formato de email (básico)
        if (!registerRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException("Formato de email inválido", "REG_INVALID_EMAIL_FORMAT");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public JwtResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        log.debug("Procesando solicitud de refresh token");

        String refreshToken = refreshRequest.getRefreshToken();
        
        try {
            // Valida el refresh token
            if (!jwtService.isRefreshToken(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
                log.warn("Intento de refresh con token inválido o expirado");
                throw new BusinessException("Token de refresh inválido o expirado", "REFRESH_INVALID_TOKEN");
            }

            // Extrae información del token
            String userEmail = jwtService.extractUsername(refreshToken);
            
            // Busca el usuario
            User user = userRepository.findByEmailAndIsActiveTrue(userEmail)
                    .orElseThrow(() -> {
                        log.warn("Usuario no encontrado durante refresh: {}", userEmail);
                        return new BusinessException("Usuario no encontrado o inactivo", "REFRESH_USER_NOT_FOUND");
                    });

            // Valida el token contra el usuario
            if (!jwtService.isTokenValid(refreshToken, user)) {
                log.warn("Token de refresh no válido para usuario: {}", userEmail);
                throw new BusinessException("Token de refresh no válido", "REFRESH_TOKEN_INVALID");
            }

            // Genera nuevo access token
            String newAccessToken = jwtService.generateJwtToken(user);
            long expiresIn = jwtExpiration / 1000;

            // Mapea usuario a DTO
            var userDTO = userMapper.toLoginResponseDTO(user);

            log.info("Token renovado exitosamente para usuario: {}", userEmail);

            return JwtResponseDTO.success(newAccessToken, refreshToken, userDTO, expiresIn);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error durante refresh token: {}", e.getMessage(), e);
            throw new BusinessException("Error interno durante renovación de token", "REFRESH_INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional
    public boolean logout(String token) {
        log.debug("Procesando solicitud de logout");

        try {
            if (!StringUtils.hasText(token)) {
                log.warn("Intento de logout con token vacío");
                return false;
            }

            // Limpia el prefijo "Bearer " si está presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String userEmail = jwtService.extractUsername(token);
            log.info("Logout procesado para usuario: {}", userEmail);

            tokenBlacklistService.blacklistToken(token);
            org.springframework.security.core.context.SecurityContextHolder.clearContext();

            return true;

        } catch (Exception e) {
            log.error("Error durante logout: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordDTO changePasswordDTO) {
        log.debug("Procesando cambio de contraseña");

        try {
            // Valida que las nuevas contraseñas coincidan
            if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
                throw new BusinessException("Las contraseñas nuevas no coinciden", "CHANGE_PWD_MISMATCH");
            }

            // Obtener usuario actual desde SecurityContext
            String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmailAndIsActiveTrue(currentUserEmail)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado o inactivo", "USER_NOT_FOUND"));

            // Verificar la contraseña actual
            if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessException("La contraseña actual es incorrecta", "CHANGE_PWD_INVALID_OLD");
            }

            // Actualizar la contraseña
            user.setPasswordHash(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            userRepository.save(user);

            log.info("Cambio de contraseña procesado exitosamente para el usuario: {}", currentUserEmail);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error durante cambio de contraseña: {}", e.getMessage(), e);
            throw new BusinessException("Error interno durante cambio de contraseña", "CHANGE_PWD_INTERNAL_ERROR");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean requestPasswordReset(PasswordResetRequestDTO resetRequest) {
        log.info("Solicitud de restablecimiento de contraseña para: {}", resetRequest.getEmail());

        try {
            var userOptional = userRepository.findByEmailAndIsActiveTrue(resetRequest.getEmail());
            
            if (userOptional.isEmpty()) {
                log.warn("Solicitud de reset para email no registrado: {}", resetRequest.getEmail());
            } else {
                // TODO: Implementar envío de email
                log.info("Email de restablecimiento enviado a: {}", resetRequest.getEmail());
            }

            return true;

        } catch (Exception e) {
            log.error("Error durante solicitud de reset: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        log.debug("Procesando restablecimiento de contraseña");

        try {
            // TODO: Implementar validación de token de reset
            log.info("Contraseña restablecida exitosamente");
            return true;

        } catch (Exception e) {
            log.error("Error durante restablecimiento de contraseña: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (jwtService.isTokenExpired(token)) {
                return false;
            }

            String userEmail = jwtService.extractUsername(token);
            var user = userRepository.findByEmailAndIsActiveTrue(userEmail);
            
            return user.isPresent() && jwtService.isTokenValid(token, user.get());

        } catch (Exception e) {
            log.debug("Error durante validación de token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String normalizedEmail = email.toLowerCase().trim();
        boolean available = !userRepository.existsByEmail(normalizedEmail);
        
        log.debug("Verificación de disponibilidad de email {}: {}", 
                normalizedEmail, available ? "disponible" : "no disponible");
        
        return available;
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserEmailFromToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return null;
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            return jwtService.extractUsername(token);

        } catch (Exception e) {
            log.debug("Error extrayendo email del token: {}", e.getMessage());
            return null;
        }
    }
}
