package com.example.springbootecommerce.notification.service;

import com.example.springbootecommerce.order.entity.Order;

public interface EmailService {

    /**
     * Envía confirmación de orden
     */
    void sendOrderConfirmation(Order order);

    /**
     * Envía actualización de estado de orden
     */
    void sendOrderStatusUpdate(Order order);

    /**
     * Envía email de bienvenida a nuevo usuario
     */
    void sendWelcomeEmail(String userEmail, String firstName);

    /**
     * Envía email de restablecimiento de contraseña
     */
    void sendPasswordResetEmail(String userEmail, String resetToken);

}
