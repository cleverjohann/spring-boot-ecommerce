package com.example.springbootecommerce.shared.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * Implementación de AuditorAware para obtener el usuario actual.
 * Se utiliza para poblamiento automático de campos de auditoría.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails){
            return Optional.of(userDetails.getUsername());
        }

        return Optional.of(authentication.getPrincipal().toString());
    }
}
