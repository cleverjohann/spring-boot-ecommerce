package com.example.springbootecommerce.user.repository;

import com.example.springbootecommerce.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio para la gestión de roles en el sistema.
 * Proporciona operaciones CRUD y consultas específicas para roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca roles que no tengan usuarios asignados (huérfanos)
     *
     * @return Lista de roles sin usuarios
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    /**
     * Cuenta el número de usuarios por rol
     *
     * @param roleName Nombre del rol
     * @return Número de usuarios con ese rol
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersByRoleName(@Param("roleName") String roleName);

    /**
     * Obtiene roles por una lista de nombres
     *
     * @param roleNames Lista de nombres de roles
     * @return Set de roles encontrados
     */
    @Query("SELECT r FROM Role r WHERE r.name IN :roleNames")
    Set<Role> findByNameIn(@Param("roleNames") List<String> roleNames);

    /**
     * Busca el rol por defecto (ROLE_USER)
     *
     * @return Optional con el rol de usuario por defecto
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'ROLE_USER'")
    Optional<Role> findDefaultUserRole();

    /**
     * Busca el rol de administrador (ROLE_ADMIN)
     *
     * @return Optional con el rol de administrador
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'ROLE_ADMIN'")
    Optional<Role> findAdminRole();

    /**
     * Verifica si un rol puede ser eliminado (no es un rol del sistema)
     *
     * @param roleName Nombre del rol
     * @return true si puede ser eliminado
     */
    @Query("SELECT CASE WHEN r.name NOT IN ('ROLE_USER', 'ROLE_ADMIN') THEN true ELSE false END " +
            "FROM Role r WHERE r.name = :roleName")
    Boolean canRoleBeDeleted(@Param("roleName") String roleName);

    /**
     * Busca un rol por su nombre.
     *
     * @param name Nombre del rol
     * @return Optional con el rol encontrado
     */
    Optional<Role> findByName(String name);


    /**
     * Verifica si existe un rol con el nombre especificado.
     *
     * @param name Nombre del rol
     * @return true si existe
     */
    boolean existsByName(String name);


    /**
     * Busca roles por nombre que contenga el texto dado (búsqueda parcial)
     *
     * @param namePattern Patrón de búsqueda
     * @return Lista de roles que coinciden
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    List<Role> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern);

    /**
     * Obtiene todos los roles ordenados por nombre
     *
     * @return Lista de roles ordenada
     */
    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    List<Role> findAllOrderByName();

    /**
     * Busca roles que tengan usuarios asignados
     *
     * @return Lista de roles con usuarios
     */
    @Query("SELECT DISTINCT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();
}
