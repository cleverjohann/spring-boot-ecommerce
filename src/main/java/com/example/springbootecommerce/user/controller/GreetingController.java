package com.example.springbootecommerce.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/greetings")
public class GreetingController {

    /**
     * Un endpoint de prueba simple para verificar si la autenticación JWT funciona.
     * Si el token es válido, debería devolver un saludo con el email del usuario.
     *
     * @param principal El objeto Principal inyectado por Spring Security.
     * @return Un saludo personalizado.
     */
    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> sayHello(Principal principal) {
        // Si principal es null, significa que el usuario no está autenticado.
        // @PreAuthorize debería haberlo prevenido, pero es una doble verificación.
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not Authenticated"));
        }
        
        String username = principal.getName(); // Esto devolverá el email del usuario (el "username" en UserDetails)
        Map<String, String> response = Map.of(
                "message", "Hello, " + username + "!",
                "description", "Your JWT token is valid and you have the role 'USER'."
        );
        return ResponseEntity.ok(response);
    }
}
