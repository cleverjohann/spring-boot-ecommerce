package com.example.springbootecommerce.user.repository;

import com.example.springbootecommerce.user.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de direcciones de usuarios.
 * Proporciona operaciones CRUD y consultas específicas para direcciones.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Busca todas las direcciones de un usuario específico
     *
     * @param userId ID del usuario
     * @return Lista de direcciones del usuario
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt ASC")
    List<Address> findByUserIdOrderByDefaultFirst(@Param("userId") Long userId);

    /**
     * Busca la dirección por defecto de un usuario
     *
     * @param userId ID del usuario
     * @return Optional con la dirección por defecto
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);

    /**
     * Busca direcciones por ciudad
     *
     * @param city Nombre de la ciudad
     * @return Lista de direcciones en esa ciudad
     */
    List<Address> findByCityIgnoreCase(String city);

    /**
     * Busca direcciones por país
     *
     * @param country Nombre del país
     * @return Lista de direcciones en ese país
     */
    List<Address> findByCountryIgnoreCase(String country);

    /**
     * Busca direcciones que pertenezcan a un usuario y una ciudad específica
     *
     * @param userId ID del usuario
     * @param city Nombre de la ciudad
     * @return Lista de direcciones del usuario en esa ciudad
     */
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND LOWER(a.city) = LOWER(:city)")
    List<Address> findByUserIdAndCity(@Param("userId") Long userId, @Param("city") String city);

    /**
     * Cuenta las direcciones de un usuario
     *
     * @param userId ID del usuario
     * @return Número de direcciones del usuario
     */
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Verifica si una dirección pertenece a un usuario específico
     *
     * @param addressId ID de la dirección
     * @param userId ID del usuario
     * @return true si la dirección pertenece al usuario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Address a WHERE a.id = :addressId AND a.user.id = :userId")
    boolean existsByIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * Busca direcciones de empresas (que tienen campo company)
     *
     * @return Lista de direcciones empresariales
     */
    @Query("SELECT a FROM Address a WHERE a.company IS NOT NULL AND a.company != ''")
    List<Address> findBusinessAddresses();

    /**
     * Busca direcciones por código postal
     *
     * @param postalCode Código postal
     * @return Lista de direcciones con ese código postal
     */
    List<Address> findByPostalCodeIgnoreCase(String postalCode);

    /**
     * Remueve el estado de por defecto de todas las direcciones de un usuario
     *
     * @param userId ID del usuario
     * @return Número de direcciones actualizadas
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    int removeDefaultStatusForUser(@Param("userId") Long userId);

    /**
     * Establece una dirección específica como por defecto para un usuario
     *
     * @param addressId ID de la dirección
     * @param userId ID del usuario
     * @return Número de direcciones actualizadas
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = true " +
            "WHERE a.id = :addressId AND a.user.id = :userId")
    int setAsDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * Busca direcciones que coincidan parcialmente con una dirección dada
     *
     * @param street Calle
     * @param city Ciudad
     * @param postalCode Código postal
     * @return Lista de direcciones similares
     */
    @Query("SELECT a FROM Address a WHERE " +
            "LOWER(a.street) LIKE LOWER(CONCAT('%', :street, '%')) AND " +
            "LOWER(a.city) = LOWER(:city) AND " +
            "a.postalCode = :postalCode")
    List<Address> findSimilarAddresses(@Param("street") String street,
                                       @Param("city") String city,
                                       @Param("postalCode") String postalCode);

    /**
     * Elimina todas las direcciones de un usuario
     *
     * @param userId ID del usuario
     * @return Número de direcciones eliminadas
     */
    @Modifying
    @Query("DELETE FROM Address a WHERE a.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Busca direcciones creadas recientemente (útil para auditoría)
     *
     * @return Lista de direcciones ordenadas por fecha de creación descendente
     */
    @Query("SELECT a FROM Address a ORDER BY a.createdAt DESC")
    List<Address> findRecentAddresses();

    /**
     * Obtiene estadísticas de direcciones por país
     *
     * @return Lista de objetos con país y conteo
     */
    @Query("SELECT a.country as country, COUNT(a) as count " +
            "FROM Address a GROUP BY a.country ORDER BY COUNT(a) DESC")
    Page<CountryStatistics> getAddressStatisticsByCountry(Pageable pageable);

    /**
     * Obtiene estadísticas de direcciones por ciudad
     *
     * @return Lista de objetos con ciudad y conteo
     */
    @Query("SELECT a.city as city, a.country as country, COUNT(a) as count " +
            "FROM Address a GROUP BY a.city, a.country ORDER BY COUNT(a) DESC")
    List<CityStatistics> getAddressStatisticsByCity();

    // ========================================================================
    // INTERFACES PARA PROYECCIONES
    // ========================================================================

    /**
     * Proyección para estadísticas por país
     */
    interface CountryStatistics {
        String getCountry();
        Long getCount();
    }

    /**
     * Proyección para estadísticas por ciudad
     */
    interface CityStatistics {
        String getCity();
        String getCountry();
        Long getCount();
    }

    /**
     * Proyección para dirección resumida
     */
    interface AddressSummary {
        Long getId();
        String getStreet();
        String getCity();
        String getCountry();
        Boolean getIsDefault();
    }

    /**
     * Obtiene un resumen de las direcciones de un usuario
     *
     * @param userId ID del usuario
     * @return Lista de direcciones resumidas
     */
    @Query("SELECT a.id as id, a.street as street, a.city as city, " +
            "a.country as country, a.isDefault as isDefault " +
            "FROM Address a WHERE a.user.id = :userId " +
            "ORDER BY a.isDefault DESC, a.createdAt ASC")
    List<AddressSummary> findAddressSummaryByUserId(@Param("userId") Long userId);
}
