package com.example.springbootecommerce.notification.service.impl;

import com.example.springbootecommerce.notification.service.EmailService;
import com.example.springbootecommerce.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Async
    @Override
    public void sendOrderConfirmation(Order order) {
        log.info("Enviando confirmación de order ID: {} a: {}",
                order.getId(), order.getUser().getEmail());
        // TODO: enviar email de confirmación de order con Spring Email

        try {
            // Simular envío de email
            Thread.sleep(1000);
            log.info("Email enviado correctamente a: {}", order.getUser().getEmail());
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.error("Error al enviar email de confirmación de order: {}", e.getMessage());
        }

    }
    @Async
    @Override
    public void sendOrderStatusUpdate(Order order) {
        log.info("Enviando actualización de estado de orden ID: {} a: {}",
                order.getId(), order.getUser().getEmail());

        try {
            // Simular envío de email
            Thread.sleep(500);
            log.info("Actualización de estado enviada exitosamente");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error enviando actualización de estado: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String userEmail, String firstName) {
        log.info("Enviando bienvenida a: {}", userEmail);

        try {
            Thread.sleep(500);
            log.info("Email de bienvenida enviado exitosamente");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error enviando email de bienvenida: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String userEmail, String resetToken) {
        log.info("Enviando Email de restablecimiento de contraseña a: {}", userEmail);

        try {
            Thread.sleep(500);
            log.info("Email de restablecimiento enviado exitosamente");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error enviando email de restablecimiento: {}", e.getMessage());
        }
    }
}
