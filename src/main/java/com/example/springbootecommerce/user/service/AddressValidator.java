package com.example.springbootecommerce.user.service;

import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.user.entity.Address;
import com.example.springbootecommerce.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressValidator {

    private final AddressRepository addressRepository;

    /**
     * Válida que una dirección exista, pertenezca al usuario y esté completa.
     * Devuelve la ENTIDAD Address si es correcto.
     */
    public Address validateUserAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Dirección de envío no encontrada"));

        if (!address.getUser().getId().equals(userId)){
            throw new BusinessException(("El usuario no esta autorizado para usar esta dirección."));
        }

        if (!address.isComplete()){
            throw new BusinessException(("La dirección de envío seleccionada no es completa."));
        }
        return address;
    }
}
