package com.example.springbootecommerce.user.repository;

import com.example.springbootecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsActiveTrue(String email);

    List<User> findAllByIsActiveTrue();

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    long countByIsActiveTrue();

    long countByRoleName(String roleName);

    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > 1")
    List<User> findUsersWithMultipleRoles();

    @Query("SELECT u FROM User u WHERE SIZE(u.addresses) = 0")
    List<User> findUsersWithoutAddresses();

    @Query("SELECT u FROM User u WHERE u.createdAt >= :sinceDate ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("sinceDate") LocalDateTime sinceDate);

    @Query(value = """
        SELECT u.id as id,
               u.email as email,
               u.first_name as firstName,
               u.last_name as lastName,
               COUNT(DISTINCT o.id) as orderCount,
               COALESCE(SUM(o.total_amount), 0) as totalSpent
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.is_active = true
        GROUP BY u.id
        ORDER BY totalSpent DESC
        """, nativeQuery = true)
    List<UserStatisticsProjection> findUserStatistics();

//    @Query("SELECT u FROM User u WHERE NOT EXISTS " +
//            "(SELECT o FROM Order o WHERE o.user = u)")
//    List<User> findUsersWithoutOrders();

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE " +
            "u.updatedAt < :inactiveDate AND u.isActive = true")
    int deactivateInactiveUsers(@Param("inactiveDate") LocalDateTime inactiveDate);
}
