package com.example.springbootecommerce.user.controller;

import com.example.springbootecommerce.shared.security.JwtService;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    // Test configuration class to provide mock beans and a test-specific security filter chain.
    static class TestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        // Provide mocks for security dependencies that might be pulled in by auto-configuration
        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        UserDetailsService userDetailsService() {
            // This mock is needed to satisfy the dependency of the security auto-configuration
            return Mockito.mock(UserDetailsService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    // Define security rules for the test environment.
                    // @WithMockUser provides the authentication context for these rules.
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/users/me").authenticated()
                            .requestMatchers("/api/v1/users/{userId}").hasRole("ADMIN")
                            .anyRequest().denyAll() // Explicitly deny other requests
                    );
            return http.build();
        }
    }
    // Prueba que el endpoint /me devuelve el perfil del usuario autenticado correctamente
    @Test
    @WithMockUser // Authenticated as a regular user with ROLE_USER by default
    void getCurrentUserProfile_ShouldReturnProfile_WhenUserIsAuthenticated() throws Exception {
        // Arrange: simula el usuario autenticado y el servicio
        // Act: realiza la petición GET al endpoint /users/me
        // Assert: verifica que la respuesta contiene los datos esperados
        UserDTO mockUser = UserDTO.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Collections.singleton("ROLE_USER"))
                .build();
        when(userService.getCurrentUserProfile()).thenReturn(mockUser);

        // The controller wraps this response in ApiResponse
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Authenticated as an admin
    void getUserProfile_ShouldReturnProfile_WhenUserIsAdmin() throws Exception {
        // Arrange: simula el usuario admin y el servicio
        // Act: realiza la petición GET al endpoint /users/{userId}
        // Assert: verifica que la respuesta contiene el perfil solicitado
        long userIdToFetch = 2L;
        UserDTO mockUser = UserDTO.builder()
                .id(userIdToFetch)
                .email("otheruser@example.com")
                .firstName("Other")
                .lastName("User")
                .roles(Collections.singleton("ROLE_USER"))
                .build();
        when(userService.getUserById(userIdToFetch)).thenReturn(mockUser);

        // This controller method does NOT wrap the response in ApiResponse
        mockMvc.perform(get("/api/v1/users/{userId}", userIdToFetch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("otheruser@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER") // Authenticated as a regular user
    void getUserProfile_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        long userIdToFetch = 2L;

        mockMvc.perform(get("/api/v1/users/{userId}", userIdToFetch))
                .andExpect(status().isForbidden());
    }
}
