package com.example.springbootecommerce.user.entity;

import com.example.springbootecommerce.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entidad que representa las direcciones de los usuarios.
 * Almacena información de direcciones de facturación y envío.
 */
@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_addresses_user_id", columnList = "user_id"),
        @Index(name = "idx_addresses_default", columnList = "user_id, isDefault")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"user"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Address extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "street", nullable = false,length = 255)
    private String street;

    @Column(name = "city", nullable = false,length = 100)
    private String city;

    @Column(name = "state", nullable = false,length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false,length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false,length = 100)
    private String country;

    @Column(name = "is_default",nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    //Campos adicionales opcionales
    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "phone", length = 20)
    private String phoneNumber;

    @Column(name = "additional_info", length = 500)
    private String additionalInfo;

    // ========================================================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================================================

    public String getFullAddress(){
        return street +
                ", " + city +
                ", " + state +
                ", " + postalCode +
                ", " + country;
    }

    public boolean isDefaultAddress(){
        return isDefault != null && isDefault;
    }

    public void setAsDefault(){
        if (user != null) {
            user.getAddresses().forEach(address-> {
                if (!address.equals(this)){
                    address.setIsDefault(false);
                }
            });
        }
        this.isDefault = true;
    }

    public static Address createBasicAddress(String street, String city, String state,
                                             String postalCode, String country) {
        return Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country)
                .isDefault(false)
                .build();
    }

    @PrePersist
    private void prePersist(){
        if(isDefault == null){
            isDefault = false;
        }
        //Si esta es la primera dirección del usuario, hacer por defecto
        if (user != null && user.getAddresses().isEmpty()) {
            isDefault = true;
        }
    }

    @PreUpdate
    private void preUpdate(){
        if (isDefault != null && isDefault && user != null) {
            //Si se marca como default, asegurarse de que sea la única
            user.getAddresses().stream()
                    .filter(address -> !address.equals(this) && address.isDefault)
                    .forEach(address -> address.setIsDefault(false));
        }
    }

}
