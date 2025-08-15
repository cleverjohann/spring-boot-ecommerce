package com.example.springbootecommerce.user.service;

import com.example.springbootecommerce.shared.dto.PageResponse;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.shared.exception.UnauthorizedOperationException;
import com.example.springbootecommerce.user.dto.AddressDTO;
import com.example.springbootecommerce.user.dto.CreatedAddressDTO;
import com.example.springbootecommerce.user.dto.UpdateUserDTO;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz del servicio de usuarios que define las operaciones de negocio.
 * Sigue el principio de segregación de interfaces (ISP) y abstracción.
 */
public interface UserService {
    // ========================================================================
    // OPERACIONES BÁSICAS DE USUARIO
    // ========================================================================

    /**
     * Obtiene un usuario por ID
     *
     * @param userId ID del usuario
     * @return UserDTO con la información del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    UserDTO getUserById(Long userId);

    /**
     * Obtiene un usuario por email
     *
     * @param email Email del usuario
     * @return UserDTO con la información del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    UserDTO getUserByEmail(String email);

    /**
     * Obtiene el perfil del usuario actualmente autenticado
     *
     * @return UserDTO con la información del usuario
     */
    UserDTO getCurrentUserProfile();

    /**
     * Actualiza el perfil del usuario actualmente autenticado
     *
     * @param updateUserDTO Datos a actualizar
     * @return UserDTO actualizado
     * @throws BusinessException si los datos son inválidos
     */
    UserDTO updateCurrentUserProfile(UpdateUserDTO updateUserDTO);

    /**
     * Desactiva el usuario actual (soft delete)
     *
     * @return true si se desactivó correctamente
     */
    boolean deactivateCurrentUser();

    /**
     * Cambia la contraseña del usuario actual
     *
     * @param currentPassword Contraseña actual
     * @param newPassword     Nueva contraseña
     * @return true si se cambió correctamente
     * @throws BusinessException si la contraseña actual es incorrecta
     */
    boolean changePassword(String currentPassword, String newPassword);

    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    /**
     * Obtiene todos los usuarios activos con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    PageResponse<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Busca usuarios por nombre, email u otros criterios usando especificaciones.
     *
     * @param search   Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de usuarios que coinciden con la búsqueda
     */
    PageResponse<UserDTO> searchUsers(String search, Pageable pageable);

    /**
     * Activa un usuario (solo administradores)
     *
     * @param userId ID del usuario a activar
     * @return UserDTO actualizado
     * @throws ResourceNotFoundException      si el usuario no existe
     * @throws UnauthorizedOperationException si no tiene permisos
     */
    UserDTO activateUser(Long userId);

    /**
     * Desactiva un usuario (solo administradores)
     *
     * @param userId ID del usuario a desactivar
     * @return UserDTO actualizado
     * @throws ResourceNotFoundException      si el usuario no existe
     * @throws UnauthorizedOperationException si no tiene permisos
     */
    UserDTO deactivateUser(Long userId);

    // ========================================================================
    // GESTIÓN DE DIRECCIONES
    // ========================================================================

    /**
     * Obtiene todas las direcciones del usuario actual
     *
     * @return Lista de direcciones
     */
    List<AddressDTO> getCurrentUserAddresses();

    /**
     * Obtiene la dirección por defecto del usuario actual
     *
     * @return AddressDTO de la dirección por defecto
     * @throws ResourceNotFoundException si no tiene dirección por defecto
     */
    AddressDTO getCurrentUserDefaultAddress();

    /**
     * Añade una nueva dirección al usuario actual
     *
     * @param createdAddressDTO Datos de la nueva dirección
     * @return AddressDTO de la dirección creada
     * @throws BusinessException si los datos son inválidos
     */
    AddressDTO addAddressToCurrentUser(CreatedAddressDTO createdAddressDTO);

    /**
     * Actualiza una dirección existente del usuario actual
     *
     * @param addressId         ID de la dirección
     * @param updatedAddressDTO Datos actualizados
     * @return AddressDTO actualizada
     * @throws ResourceNotFoundException      si la dirección no existe
     * @throws UnauthorizedOperationException si no es del usuario actual
     */
    AddressDTO updateCurrentUserAddress(Long addressId, CreatedAddressDTO updatedAddressDTO);

    /**
     * Establece una dirección como por defecto
     *
     * @param addressId ID de la dirección
     * @return AddressDTO actualizada
     * @throws ResourceNotFoundException      si la dirección no existe
     * @throws UnauthorizedOperationException si no es del usuario actual
     */
    AddressDTO setDefaultAddress(Long addressId);

    /**
     * Elimina una dirección del usuario actual
     *
     * @param addressId ID de la dirección a eliminar
     * @throws ResourceNotFoundException      si la dirección no existe
     * @throws UnauthorizedOperationException si no es del usuario actual
     * @throws BusinessException              si es la única dirección y hay órdenes pendientes
     */
    void deleteCurrentUserAddress(Long addressId);

    // ========================================================================
    // OPERACIONES DE VALIDACIÓN Y UTILIDADES
    // ========================================================================

    /**
     * Verifica si un email ya está en uso
     *
     * @param email Email a verificar
     * @return true si el email está disponible
     */
    boolean isEmailAvailable(String email);

    /**
     * Verifica si un email ya está en uso (excluyendo un usuario específico)
     *
     * @param email         Email a verificar
     * @param excludeUserId ID del usuario a excluir de la verificación
     * @return true si el email está disponible
     */
    boolean isEmailAvailable(String email, Long excludeUserId);

    /**
     * Obtiene estadísticas del usuario
     *
     * @param userId ID del usuario
     * @return UserDTO con estadísticas incluidas
     */
    UserDTO getUserWithStatistics(Long userId);

    /**
     * Verifica si un usuario existe y está activo
     *
     * @param userId ID del usuario
     * @return true si existe y está activo
     */
    boolean existsAndIsActive(Long userId);

    /**
     * Obtiene la entidad User por ID (para uso interno del sistema)
     *
     * @param userId ID del usuario
     * @return Entidad User
     * @throws ResourceNotFoundException si no existe
     */
    User getUserEntityById(Long userId);

    /**
     * Obtiene el usuario actual como entidad (para uso interno)
     *
     * @return Entidad User del usuario autenticado
     */
    User getCurrentUserEntity();

    // ========================================================================
    // OPERACIONES EN LOTE Y REPORTES
    // ========================================================================

    /**
     * Obtiene usuarios registrados recientemente
     *
     * @param days Número de días hacia atrás
     * @return Lista de usuarios recientes
     */
    List<UserDTO> getRecentUsers(int days);

    /**
     * Obtiene estadísticas generales de usuarios
     *
     * @return Mapa con estadísticas (total usuarios, activos, por rol, etc.)
     */
    UserStatistics getUserStatistics();

    /**
     * DTO para estadísticas de usuarios
     */
    record UserStatistics(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long adminUsers,
            long regularUsers,
            long usersWithAddresses
    ) {
    }
}
