package com.example.springbootecommerce.user.service.impl;

import com.example.springbootecommerce.shared.dto.PageResponse;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.DuplicateResourceException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.shared.exception.UnauthorizedOperationException;
import com.example.springbootecommerce.shared.util.Constants;
import com.example.springbootecommerce.user.dto.AddressDTO;
import com.example.springbootecommerce.user.dto.CreatedAddressDTO;
import com.example.springbootecommerce.user.dto.UpdateUserDTO;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.entity.Address;
import com.example.springbootecommerce.user.entity.User;
import com.example.springbootecommerce.user.mapper.AddressMapper;
import com.example.springbootecommerce.user.mapper.UserMapper;
import com.example.springbootecommerce.user.repository.UserRepository;
import com.example.springbootecommerce.user.service.UserService;
import com.example.springbootecommerce.user.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Implementación del servicio de usuarios.
 * Contiene toda la lógica de negocio relacionada con usuarios.
 * Sigue principios SOLID: SRP, OCP, DIP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    // ========================================================================
    // OPERACIONES BÁSICAS DE USUARIO
    // ========================================================================

    @Override
    public UserDTO getUserById(Long userId) {
        log.debug("Obteniendo usuario por ID: {}", userId);

        User user = getUserEntityById(userId);
        UserDTO userDTO = userMapper.toUserDTO(user);

        log.debug("Obteniendo usuario por DTO: {}", userDTO);
        return userDTO;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        log.debug("Obteniendo usuario por email: {}", email);

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        UserDTO userDTO = userMapper.toUserDTO(user);

        log.debug("Usuario encontrado: {}", userMapper.toUserSummaryDTO(user));
        return userDTO;
    }

    @Override
    public UserDTO getCurrentUserProfile() {
        log.debug("Obteniendo perfil del usuario actual");

        User currentUser = getCurrentUserEntity();
        UserDTO userDTO = userMapper.toUserDTO(currentUser);

        log.debug("Perfil del usuario actual: {}", currentUser.getEmail());
        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUserProfile(UpdateUserDTO updateUserDTO) {
        log.info("Actualizando perfil del usuario actual");

        // Validaciones preliminares
        if (!updateUserDTO.hasUpdates()) {
            throw new BusinessException("No hay datos para actualizar");
        }

        if (!updateUserDTO.isValid()) {
            throw new BusinessException("Datos inválidos para actualizar el perfil");
        }

        User currentUser = getCurrentUserEntity();

        // Verificar contraseña actual si es necesaria
        if (updateUserDTO.requiresCurrentPassword()) {
            validateCurrentPassword(currentUser, updateUserDTO.getCurrentPassword());
        }

        // Verificar disponibilidad de email si se está cambiando
        if (updateUserDTO.isChangingEmail()) {
            validateEmailAvailability(updateUserDTO.getEmail(), currentUser.getId());
        }

        // Normalizar datos
        updateUserDTO.normalize();

        // Aplicar actualizaciones
        userMapper.updateUserFromDTO(updateUserDTO, currentUser);

        // Manejar cambio de email por separado
        if (updateUserDTO.isChangingPassword()) {
            String encodedPassword = passwordEncoder.encode(updateUserDTO.getNewPassword());
            currentUser.setPasswordHash(encodedPassword);
        }

        User savedUser = userRepository.save(currentUser);
        UserDTO result = userMapper.toUserDTO(savedUser);

        log.info("Perfil actualizado exitosamente para usuario: {} - Cambios: {}",
                currentUser.getEmail(), updateUserDTO.getUpdateSummary());

        return result;
    }

    @Override
    public boolean deactivateCurrentUser() {
        log.info("Desactivando cuenta de usuario actual");

        User currentUser = getCurrentUserEntity();

        // Verifica que el usuario puede ser desactivado
        // (ej. no tiene órdenes pendientes, no es el único admin, etc.)
        validateUserCanBeDeactivated(currentUser);

        currentUser.setIsActive(false);
        userRepository.save(currentUser);

        log.info("Usuario desactivado exitosamente : {}", currentUser.getEmail());
        return true;
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        log.info("Cambio de contraseña del usuario actual");

        User currentUser = getCurrentUserEntity();

        // Validar contraseña actual
        validateCurrentPassword(currentUser, currentPassword);

        // Validar nueva contraseña
        if (newPassword.length() < 8) {
            throw new BusinessException("La nueva contraseña debe tener al menos 8 caracteres ");
        }

        // Verificar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, currentUser.getPasswordHash())) {
            throw new BusinessException("La nueva contraseña debe ser diferente a la actual");
        }

        //Cambiar contraseña
        String encodePassword = passwordEncoder.encode(newPassword);
        currentUser.setPasswordHash(encodePassword);
        userRepository.save(currentUser);

        log.info("Contraseña cambiada exitosamente para usuario: {}", currentUser.getEmail());
    }

    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    @Override
    public PageResponse<UserDTO.UserSummaryDTO> getAllUsersAndSearchUsers(String search, Pageable pageable) {
        log.debug("Buscando usuarios (resumen) con término: '{}' y paginación: {}", search, pageable);

        verifyAdminPermissions();

        Specification<User> spec = UserSpecification.isActive();
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(UserSpecification.nameContains(search.trim()));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserDTO.UserSummaryDTO> userSummaries = userPage.getContent()
                .stream()
                .map(userMapper::toUserSummaryDTO)
                .toList();

        PageResponse<UserDTO.UserSummaryDTO> response = new PageResponse<>(
                userSummaries,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.isEmpty()
        );

        log.debug("Búsqueda de usuarios (resumen) completada: {} encontrados", userPage.getTotalElements());
        return response;
    }


    @Override
    @Transactional
    public UserDTO activateUser(Long userId) {
        log.info("Activando usuario por ID: {}", userId);

        // Verificar permisos de administrador
        verifyAdminPermissions();

        User user = getUserEntityById(userId);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        log.info("Usuario activado exitosamente : {}", user.getEmail());
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO deactivateUser(Long userId) {
        log.info("Desactivamos usuario ID :{}", userId);

        //Verificamos permisos de Administrador
        verifyAdminPermissions();

        User user = getUserEntityById(userId);

        // Verificamos que no se desactive asi mismo
        User currentUser = getCurrentUserEntity();
        if (user.getId().equals(currentUser.getId())) {
            throw new BusinessException("No se puede desactivar la propia cuenta");
        }

        // Verificamos si el usuario puede ser desactivado
        validateUserCanBeDeactivated(user);

        user.setIsActive(false);
        User savedUser = userRepository.save(user);

        log.info("Usuario desactivado exitosamente por el Admin : {}", user.getEmail());
        return userMapper.toUserDTO(savedUser);
    }

    // ========================================================================
    // GESTIÓN DE DIRECCIONES
    // ========================================================================

    @Override
    public List<AddressDTO> getCurrentUserAddresses() {
        log.debug("Obteniendo direcciones del usuario actual");

        User currentUser = getCurrentUserEntity();
        List<AddressDTO> addresses = addressMapper.toAddressDTOs(currentUser.getAddresses());

        log.debug("Obteniendo direcciones del usuario actual: {}", addresses.size());
        return addresses;
    }

    @Override
    public AddressDTO getCurrentUserDefaultAddress() {
        log.debug("Obteniendo direcciones por defecto del usuario actual");

        User currentUser = getCurrentUserEntity();

        Address defaultAddress = currentUser.getAddresses().stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró dirección por defecto para el usuario"
                ));
        AddressDTO addressDTO = addressMapper.toAddressDTO(defaultAddress);

        log.debug("Dirección por defecto encontrada : {}", defaultAddress.getId());
        return addressDTO;
    }

    @Override
    @Transactional
    public AddressDTO addAddressToCurrentUser(CreatedAddressDTO createdAddressDTO) {
        log.info("Añadimos nueva dirección al usuario actual");

        // Validar y normalizar datos
        createdAddressDTO.normalize();
        if (!createdAddressDTO.isValid()) {
            throw new BusinessException("Los datos son inválidos");
        }

        User currentUser = getCurrentUserEntity();

        //Verificar límite de direcciones (ej. Maximo 5 por usuario)
        validateAddressLimit(currentUser);

        Address newAddress = addressMapper.toAddress(createdAddressDTO);

        // Sí es la primera dirección hacerla por defecto automáticamente
        if (currentUser.getAddresses().isEmpty() || createdAddressDTO.getIsDefault()){
            setAllAddressAsNonDefault(currentUser);
            newAddress.setIsDefault(true);
        }

        currentUser.addAddress(newAddress);
        User savedUser = userRepository.save(currentUser);

        // La nueva dirección es la última añadida a la lista
        Address savedAddress = savedUser.getAddresses().stream()
                .max(Comparator.comparing(Address::getId))
                .orElseThrow(() -> new BusinessException("Error al guardar la dirección: no se pudo obtener la dirección guardada."));

        AddressDTO result = addressMapper.toAddressDTO(savedAddress);

        log.info("Dirección añadida exitosamente para usuario: {} - ID: {}",
                currentUser.getEmail(), savedAddress.getId());
        return result;
    }

    @Override
    @Transactional
    public AddressDTO updateCurrentUserAddress(Long addressId, CreatedAddressDTO updatedAddressDTO) {
        log.info("Actualizando direction de usuario por ID: {}", addressId);

        // Validar y normalizar datos
        updatedAddressDTO.normalize();
        if (!updatedAddressDTO.isValid()) {
            throw new BusinessException("Los datos de la dirección son inválidos");
        }

        User currentUser = getCurrentUserEntity();

        // Buscar la dirección y verificar que pertenece al usuario actual
        Address address = currentUser.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Manejar cambio a dirección por defecto
        if (updatedAddressDTO.getIsDefault() != null && updatedAddressDTO.getIsDefault()) {
            setAllAddressAsNonDefault(currentUser);
            address.setIsDefault(true);
        }

        // Actualizar campos
        addressMapper.updateAddressFromDTO(updatedAddressDTO, address);

        User savedUser = userRepository.save(currentUser);

        // Encontrar la dirección actualizada
        Address updatedAddress = savedUser.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Error al actualizar la dirección"));

        AddressDTO result = addressMapper.toAddressDTO(updatedAddress);

        log.info("Dirección actualizada exitosamente para usuario: {} - ID: {}",
                currentUser.getEmail(), updatedAddress.getId());

        return result;
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddress(Long addressId) {
        log.info("Estableciendo dirección por defecto: {}", addressId);

        User currentUser = getCurrentUserEntity();

        // Buscar la dirección y verificar que pertenece al usuario actual
        Address address = currentUser.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Quitar el estado de por defecto a todas las direcciones
        setAllAddressAsNonDefault(currentUser);

        // Establecer la dirección como por defecto
        address.setIsDefault(true);

        User savedUser = userRepository.save(currentUser);

        // Encontrar la dirección actualizada
        Address updatedAddress = savedUser.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Error al establecer dirección por defecto"));

        AddressDTO result = addressMapper.toAddressDTO(updatedAddress);

        log.info("Dirección establecida por defecto : ID {}",addressId);

        return result;
    }

    @Override
    @Transactional
    public void deleteCurrentUserAddress(Long addressId) {
        log.info("Eliminando dirección del usuario actual: {}", addressId);

        User currentUser = getCurrentUserEntity();

        // Buscar la dirección y verificar que pertenece al usuario actual
        Address address = currentUser.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", addressId));

        // Verificar que no es la única dirección del usuario
        if (currentUser.getAddresses().size() <= 1) {
            //TODO: Verificar si hay órdenes pendientes que requieran una dirección
            // Aquí se puede verificar si hay órdenes pendientes que requieran una dirección
            // Por ahora permitimos eliminar la última dirección
            throw new BusinessException("No se puede eliminar la única dirección del usuario");
        }

        //Sí era la dirección por defecto, establecer otra como por defecto
        if (address.getIsDefault()) {
            Address newDefaultAddress = currentUser.getAddresses().stream()
                    .filter(addr -> !addr.getId().equals(addressId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("No hay otra dirección por defecto disponible"));

            if (newDefaultAddress != null){
                newDefaultAddress.setIsDefault(true);
            }
        }

        // Eliminar la dirección
        currentUser.removeAddress(address);
        userRepository.save(currentUser);
        log.info("Dirección eliminada exitosamente: ID {}", addressId);
    }

    // ========================================================================
    // MÉTODOS AUXILIARES PÚBLICOS
    // ========================================================================

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isEmailAvailable(String email, Long excludeUserId) {
        User existingUser = userRepository.findByEmail(email).orElse(null);
        return existingUser == null || existingUser.getId().equals(excludeUserId);
    }

    @Override
    public UserDTO getUserWithStatistics(Long userId) {
        log.debug("Obteniendo usuario con estadísticas por ID: {}", userId);

        User user = getUserEntityById(userId);
        UserDTO userDTO = userMapper.toUserDTO(user);
        //TODO: Aquí se pueden agregar estadísticas adicionales al UserDTO
        // Aquí se pueden agregar estadísticas adicionales
        // Por ejemplo, número de órdenes, total gastado, numero de reseñas, etc.
        return userDTO;
    }

    @Override
    public boolean existsAndIsActive(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsActive)
                .orElse(false);
    }

    @Override
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("Usuario", "id", userId));
    }

    @Override
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedOperationException("No hay un usuario autenticado");
        }

        String email = authentication.getName();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(()-> new UnauthorizedOperationException("Usuario autenticado no encontrado o inactivo"));
    }

    // ========================================================================
    // OPERACIONES EN LOTE Y REPORTES
    // ========================================================================

    @Override
    public List<UserDTO> getRecentUsers(int days) {
        log.debug("Obteniendo usuarios registrados en los últimos {} días", days);

        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        List<User> recentUsers = userRepository.findRecentUsers(sinceDate);

        log.debug("Usuarios recientes encontrados: {}", recentUsers.size());

        return userMapper.toUserDTOs(recentUsers);
    }

    @Override
    public UserStatistics getUserStatistics() {
        log.debug("Obteniendo estadísticas de usuarios");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;
        long adminUsers = userRepository.countByRoles_Name(Constants.ROLE_ADMIN);
        long regularUsers = userRepository.countByRoles_Name(Constants.ROLE_USER);

        List<User> usersWithMultipleRoles = userRepository.findUsersWithMultipleRoles();
        long usersWithMultipleRolesCount = usersWithMultipleRoles.size();

        //TODO: Implementar lógica para calcular estadísticas adicionales
        // Calculamos promedio de órdenes por usuario (requiere acceso a repositorio de órdenes)
//        double averageOrdersPerUser = 0.0; // Aquí se puede implementar lógica para calcular

        UserStatistics statistics = new UserStatistics(
                totalUsers,
                activeUsers,
                inactiveUsers,
                adminUsers,
                regularUsers,
                usersWithMultipleRolesCount
        );

        log.debug("Estadísticas calculadas: Total={}, Activos={}, Admins={}",
                totalUsers, activeUsers, adminUsers);
        return statistics;
    }

    // ========================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================================================

    private void validateCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new BusinessException("Se requiere la contraseña actual para este cambio");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }
    }

    private void validateEmailAvailability(String email, Long excludeUserId) {
        if (!isEmailAvailable(email, excludeUserId)) {
            throw new DuplicateResourceException("Usuario", "email", email);
        }
    }

    private void validateUserCanBeDeactivated(User user) {
        if (user.hasRole(Constants.ROLE_ADMIN)) {
            long adminCount = userRepository.countByRoles_Name(Constants.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new BusinessException("No se puede desactivar el único administrador del sistema");
            }
        }

        //TODO: Aquí se pueden agregar más validaciones según las reglas de negocio
        // Aquí podemos añadir más validaciones
        // - Verificar que no tiene órdenes pendientes
        // - Verificar que no tiene pagos en proceso
    }

    private void setAllAddressAsNonDefault(User user) {
        user.getAddresses().forEach(address -> address.setIsDefault(false));
    }

    private void verifyAdminPermissions() {
        User currentUser = getCurrentUserEntity();
        if (!currentUser.hasRole(Constants.ROLE_ADMIN)) {
            throw new UnauthorizedOperationException(
                    "Se requiere permisos administrativos para esta operación"
            );
        }
    }

    private void validateAddressLimit(User user) {
        final int MAX_ADDRESS = 5;
        if (user.getAddresses().size() >= MAX_ADDRESS) {
            throw new BusinessException(
                    String.format("No se puede tener más de %d direcciones", MAX_ADDRESS)
            );
        }
    }
}
