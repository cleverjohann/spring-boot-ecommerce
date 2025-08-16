package com.example.springbootecommerce.user.controller;

import com.example.springbootecommerce.auth.dto.ChangePasswordDTO;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import com.example.springbootecommerce.user.dto.UpdateUserDTO;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Obtener perfil de usuario actual", description = "Obtiene el perfil de un usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(){
        log.debug("Obteniendo perfil de usuario actual");
        UserDTO userDTO = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(userDTO,"Peril obtenido exitosamente"));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil de usuario actual", description = "Actualiza el perfil de un usuario autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUserProfile(@Valid @RequestBody UpdateUserDTO updateUserDTO){
        log.info("Actualizando perfil de usuario actual : {}", updateUserDTO.getUpdateSummary());
        UserDTO userDTO = userService.updateCurrentUserProfile(updateUserDTO);
        return ResponseEntity.ok(ApiResponse.success(userDTO,"Perfil actualizado exitosamente"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Desactivar cuenta de usuario actual", description = "Desactiva la cuenta de un usuario autenticado")
    public ResponseEntity<ApiResponse<Boolean>> deactivateCurrentUser(){
        log.info("Desactivando cuenta de usuario actual");
        Boolean result = userService.deactivateCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(result,"Cuenta desactivada exitosamente"));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request){
        log.info("Solicitando cambio de contraseña para usuario actual");
        userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // ENDPOINTS ADMINISTRATIVOS
    // ========================================================================

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener perfil de usuario", description = "Obtiene el perfil de un usuario concreto")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long userId){
        log.debug("Obteniendo perfil de usuario con ID: {}", userId);
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }
}
