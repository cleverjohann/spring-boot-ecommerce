package com.example.springbootecommerce.user.controller;

import com.example.springbootecommerce.auth.dto.ChangePasswordDTO;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import com.example.springbootecommerce.shared.dto.PageResponse;
import com.example.springbootecommerce.user.dto.AddressDTO;
import com.example.springbootecommerce.user.dto.CreatedAddressDTO;
import com.example.springbootecommerce.user.dto.UpdateUserDTO;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.springbootecommerce.shared.util.Constants.USERS_ENDPOINT;

/**
 * Controlador REST para la gestión de usuarios.
 * Proporciona endpoints para operaciones CRUD de usuarios,
 * gestión de perfiles, direcciones y operaciones administrativas.
 */

@RestController
@RequestMapping(USERS_ENDPOINT)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Users", description = "API para gestion de usuarios")
public class UserController {

    private final UserService userService;

    // ========================================================================
    // ENDPOINTS DE PERFIL DE USUARIO
    // ========================================================================

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil de usuario actual", description = "Obtiene el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        log.debug("Obteniendo perfil del usuario actual");

        UserDTO user = userService.getCurrentUserProfile();

        return ResponseEntity.ok(
                ApiResponse.success(user, "Perfil de usuario obtenido exitosamente")
        );
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil de usuario actual", description = "Actualiza el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {

        log.info("Actualizando perfil del usuario actual: {}", updateUserDTO.getUpdateSummary());

        UserDTO updatedUser = userService.updateCurrentUserProfile(updateUserDTO);

        return ResponseEntity.ok(
                ApiResponse.success(updatedUser, "Perfil actualizado exitosamente")
        );
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {

        log.info("Cambiando contraseña para usuario actual");

        userService.changePassword(changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());

        return ResponseEntity.ok(
                ApiResponse.success(null, "Contraseña cambiada exitosamente")
        );
    }

    @DeleteMapping("/me")
    @Operation(summary = "Desactivar cuenta de usuario actual", description = "Desactiva la cuenta del usuario autenticado")
    public ResponseEntity<ApiResponse<Boolean>> deactivateCurrentUser() {
        log.info("Desactivando cuenta de usuario actual");

        Boolean result = userService.deactivateCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success(result, "Cuenta desactivada exitosamente")
        );
    }

    // ========================================================================
    // GESTIÓN DE DIRECCIONES
    // ========================================================================

    @GetMapping("/me/addresses")
    @Operation(summary = "Obtener direcciones del usuario", description = "Obtiene todas las direcciones del usuario autenticado")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getUserAddresses() {

        log.debug("Obteniendo direcciones del usuario actual");

        List<AddressDTO> addresses = userService.getCurrentUserAddresses();

        return ResponseEntity.ok(
                ApiResponse.success(addresses, "Direcciones obtenidas exitosamente")
        );
    }

    @GetMapping("/me/addresses/default")
    @Operation(summary = "Obtener dirección por defecto", description = "Obtiene la dirección por defecto del usuario autenticado")
    public ResponseEntity<ApiResponse<AddressDTO>> getDefaultAddress() {

        log.debug("Obteniendo dirección por defecto del usuario actual");

        AddressDTO defaultAddress = userService.getCurrentUserDefaultAddress();

        return ResponseEntity.ok(
                ApiResponse.success(defaultAddress, "Dirección por defecto obtenida exitosamente")
        );
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Agregar nueva dirección", description = "Agrega una nueva dirección al usuario autenticado")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @Valid @RequestBody CreatedAddressDTO createdAddressDTO) {

        log.info("Agregando nueva dirección para usuario actual");

        AddressDTO newAddress = userService.addAddressToCurrentUser(createdAddressDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAddress, "Dirección agregada exitosamente"));
    }

    @PutMapping("/me/addresses/{addressId}")
    @Operation(summary = "Actualizar dirección", description = "Actualiza una dirección existente del usuario autenticado")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody CreatedAddressDTO updateAddressDTO) {

        log.info("Actualizando dirección ID: {} para usuario actual", addressId);

        AddressDTO updatedAddress = userService.updateCurrentUserAddress(addressId, updateAddressDTO);

        return ResponseEntity.ok(
                ApiResponse.success(updatedAddress, "Dirección actualizada exitosamente")
        );
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Eliminar dirección", description = "Elimina una dirección del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId) {

        log.info("Eliminando dirección ID: {} para usuario actual", addressId);

        userService.deleteCurrentUserAddress(addressId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Dirección eliminada exitosamente")
        );
    }

    @PutMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Establecer dirección por defecto", description = "Establece una dirección como por defecto para el usuario autenticado")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @PathVariable Long addressId) {

        log.info("Estableciendo dirección por defecto ID: {} para usuario actual", addressId);

        AddressDTO defaultAddress = userService.setDefaultAddress(addressId);

        return ResponseEntity.ok(
                ApiResponse.success(defaultAddress, "Dirección por defecto establecida exitosamente")
        );
    }


    // ========================================================================
    // ENDPOINTS ADMINISTRATIVOS
    // ========================================================================

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener perfil de usuario", description = "Obtiene el perfil de un usuario concreto")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(@PathVariable Long userId){
        log.debug("Obteniendo perfil de usuario con ID: {}", userId);
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userDTO, "Perfil de usuario obtenido exitosamente"));
    }

    @GetMapping("/admin/{userId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario con estadísticas", description = "Obtiene un usuario con sus estadísticas (solo administradores)")
    public ResponseEntity<ApiResponse<UserDTO>> getUserWithStatistics(@PathVariable Long userId) {
        log.debug("Obteniendo usuario con estadísticas ID: {}", userId);

        UserDTO userDTO = userService.getUserWithStatistics(userId);

        return ResponseEntity.ok(
                ApiResponse.success(userDTO, "Usuario con estadísticas obtenido exitosamente")
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene todos los usuarios con paginación y búsqueda (solo administradores)")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO.UserSummaryDTO>>> getAllUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {

        log.debug("Obteniendo usuarios con búsqueda: {} y paginación: {}", search, pageable);

        PageResponse<UserDTO.UserSummaryDTO> users = userService.getAllUsersAndSearchUsers(search, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(users, "Usuarios obtenidos exitosamente")
        );
    }

    @PutMapping("/admin/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar usuario", description = "Activa un usuario (solo administradores)")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable Long userId) {
        log.info("Activando usuario ID: {}", userId);

        UserDTO activatedUser = userService.activateUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(activatedUser, "Usuario activado exitosamente")
        );
    }

    @PutMapping("/admin/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar usuario", description = "Desactiva un usuario (solo administradores)")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateUser(@PathVariable Long userId) {
        log.info("Desactivando usuario ID: {}", userId);

        UserDTO deactivatedUser = userService.deactivateUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(deactivatedUser, "Usuario desactivado exitosamente")
        );
    }

    // ========================================================================
    // ENDPOINTS DE UTILIDADES Y VALIDACIÓN
    // ========================================================================

    @GetMapping("/validate-email")
    @Operation(summary = "Validar disponibilidad de email", description = "Verifica si un email está disponible")
    public ResponseEntity<ApiResponse<Boolean>> validateEmailAvailability(
            @RequestParam String email) {

        log.debug("Validando disponibilidad de email: {}", email);

        Boolean isAvailable = userService.isEmailAvailable(email);
        String message = isAvailable ? "Email disponible" : "Email ya está en uso";

        return ResponseEntity.ok(
                ApiResponse.success(isAvailable, message)
        );
    }

    @GetMapping("/admin/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuarios recientes", description = "Obtiene usuarios registrados recientemente (solo administradores)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getRecentUsers(
            @RequestParam(defaultValue = "7") int days) {

        log.debug("Obteniendo usuarios registrados en los últimos {} días", days);

        List<UserDTO> recentUsers = userService.getRecentUsers(days);

        return ResponseEntity.ok(
                ApiResponse.success(recentUsers, "Usuarios recientes obtenidos exitosamente")
        );
    }

}
