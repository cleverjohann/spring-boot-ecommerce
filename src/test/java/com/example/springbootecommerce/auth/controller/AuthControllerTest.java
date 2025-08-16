package com.example.springbootecommerce.auth.controller;

import com.example.springbootecommerce.auth.dto.*;
import com.example.springbootecommerce.auth.service.AuthService;
import com.example.springbootecommerce.shared.security.JwtService;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService; // Injected from TestConfig

    private UserDTO userDTO;
    private JwtResponseDTO jwtResponseDTO;

    // Test configuration class to provide mock beans
    static class TestConfig {
        @Bean
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        userDTO = UserDTO.builder()
                .id(1L)
                .email("user@ecommerce.com")
                .firstName("Test")
                .lastName("User")
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        jwtResponseDTO = JwtResponseDTO.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .user(userDTO)
                .build();
    }

    @Test
    void login_ShouldReturnJwtResponse_WhenCredentialsAreValid() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("user@ecommerce.com", "password123");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(jwtResponseDTO);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Autenticación exitosa")))
                .andExpect(jsonPath("$.data.access_token", is("accessToken")))
                .andExpect(jsonPath("$.data.user.email", is("user@ecommerce.com")));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("user@ecommerce.com", "wrongpassword");
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new AuthenticationServiceException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_ShouldReturnJwtResponse_WhenRegistrationIsSuccessful() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("Test", "User", "newuser@ecommerce.com", "password123");
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(jwtResponseDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Usuario registrado exitosamente")))
                .andExpect(jsonPath("$.data.access_token", is("accessToken")));
    }

    @Test
    void refreshToken_ShouldReturnNewJwtResponse_WhenRefreshTokenIsValid() throws Exception {
        RefreshTokenRequestDTO refreshTokenRequest = new RefreshTokenRequestDTO("validRefreshToken");
        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(jwtResponseDTO);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.access_token", is("accessToken")));
    }

    @Test
    void requestPasswordReset_ShouldReturnSuccess() throws Exception {
        PasswordResetRequestDTO request = new PasswordResetRequestDTO("user@ecommerce.com");
        when(authService.requestPasswordReset(any(PasswordResetRequestDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Solicitud procesada correctamente")));
    }

    @Test
    void resetPassword_ShouldReturnSuccess_WhenTokenIsValid() throws Exception {
        PasswordResetDTO request = new PasswordResetDTO("valid-token", "newPassword123", "newPassword123");
        when(authService.resetPassword("valid-token", "newPassword123")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Contraseña restablecida exitosamente")));
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        PasswordResetDTO request = new PasswordResetDTO("valid-token", "newPassword123", "differentPassword");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Las contraseñas no coinciden")));
    }

    @Test
    @WithMockUser
    void getCurrentUser_ShouldReturnUserInfo_WhenUserIsAuthenticated() throws Exception {
        when(authService.getUserEmailFromToken(any())).thenReturn("user@ecommerce.com");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("user@ecommerce.com")));
    }
}
