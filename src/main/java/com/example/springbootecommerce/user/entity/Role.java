package com.example.springbootecommerce.user.entity;

import com.example.springbootecommerce.shared.audit.Auditable;
import com.example.springbootecommerce.shared.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los roles del sistema.
 * Define los permisos y autoridades de los usuarios.
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_roles_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"users"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Role extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // ========================================================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================================================

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(name);
    }

    public boolean isUser() {
        return Constants.ROLE_USER.equals(name);
    }

    public static Role createUserRole() {
        return Role.builder()
                .name(Constants.ROLE_USER)
                .description("Usuario estándar con permisos básicos")
                .build();
    }

    public static Role createAdminRole() {
        return Role.builder()
                .name(Constants.ROLE_ADMIN)
                .description("Administrador con acceso completo al sistema")
                .build();
    }

    @PrePersist
    @PreUpdate
    private void prePersistUpdate() {
        if (name != null && !name.startsWith("ROLE_")) {
            throw new IllegalArgumentException("El nombre del rol debe empezar por ROLE_ prefix");
        }
    }

}
