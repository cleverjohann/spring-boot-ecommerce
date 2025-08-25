package com.example.springbootecommerce.shared.util;

/**
 * Constantes globales de la aplicación.
 * Versión consolidada que mantiene compatibilidad hacia atrás.
 */
public final class Constants {

    // ========================================================================
    // COMPATIBILIDAD HACIA ATRÁS (DEPRECATED - Usar las nuevas versiones)
    // ========================================================================

    /**
     * @deprecated Usar API_BASE_PATH
     */
    @Deprecated
    public static final String API_V1 = "/api/v1";

    /**
     * @deprecated Usar JWT_HEADER_NAME
     */
    @Deprecated
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * @deprecated Usar JWT_TOKEN_PREFIX
     */
    @Deprecated
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    // ========================================================================
    // CONSTANTES DE API - VERSIÓN ACTUALIZADA
    // ========================================================================

    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;

    // Endpoints específicos
    public static final String AUTH_ENDPOINT = API_BASE_PATH + "/auth";
    public static final String USERS_ENDPOINT = API_BASE_PATH + "/users";
    public static final String PRODUCTS_ENDPOINT = API_BASE_PATH + "/products";
    public static final String CATEGORIES_ENDPOINT = API_BASE_PATH + "/categories";
    public static final String ORDERS_ENDPOINT = API_BASE_PATH + "/orders";
    public static final String CART_ENDPOINT = API_BASE_PATH + "/cart";
    public static final String REVIEWS_ENDPOINT = API_BASE_PATH + "/reviews";
    public static final String PAYMENTS_ENDPOINT = API_BASE_PATH + "/payments";

    // ========================================================================
    // CONSTANTES DE ROLES
    // ========================================================================

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_VENDOR = "ROLE_VENDOR";

    // ========================================================================
    // CONSTANTES DE JWT
    // ========================================================================

    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_FULL_NAME = "fullName";
    public static final String JWT_TOKEN_TYPE_ACCESS = "access";
    public static final String JWT_TOKEN_TYPE_REFRESH = "refresh";

    // ========================================================================
    // CONSTANTES DE PAGINACIÓN
    // ========================================================================

    public static final int DEFAULT_PAGE_SIZE = 20; // Actualizado de 10 a 20
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // ========================================================================
    // CONSTANTES DE VALIDACIÓN
    // ========================================================================

    // Usuario
    public static final int USER_NAME_MIN_LENGTH = 2;
    public static final int USER_NAME_MAX_LENGTH = 100;
    public static final int USER_EMAIL_MAX_LENGTH = 255;
    public static final int USER_PASSWORD_MIN_LENGTH = 8;
    public static final int USER_PASSWORD_MAX_LENGTH = 100;

    // Dirección
    public static final int ADDRESS_STREET_MIN_LENGTH = 5;
    public static final int ADDRESS_STREET_MAX_LENGTH = 255;
    public static final int ADDRESS_CITY_MIN_LENGTH = 2;
    public static final int ADDRESS_CITY_MAX_LENGTH = 100;
    public static final int ADDRESS_STATE_MIN_LENGTH = 2;
    public static final int ADDRESS_STATE_MAX_LENGTH = 100;
    public static final int ADDRESS_POSTAL_CODE_MIN_LENGTH = 4;
    public static final int ADDRESS_POSTAL_CODE_MAX_LENGTH = 20;
    public static final int ADDRESS_COUNTRY_MIN_LENGTH = 2;
    public static final int ADDRESS_COUNTRY_MAX_LENGTH = 100;
    public static final int ADDRESS_COMPANY_MAX_LENGTH = 100;
    public static final int ADDRESS_PHONE_MAX_LENGTH = 20;
    public static final int ADDRESS_ADDITIONAL_INFO_MAX_LENGTH = 500;
    public static final int MAX_ADDRESSES_PER_USER = 5;

    // ========================================================================
    // CONSTANTES DE ESTADO
    // ========================================================================

    // Estados de usuario
    public static final boolean USER_ACTIVE = true;
    public static final boolean USER_INACTIVE = false;

    // Estados de órdenes
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_PROCESSING = "PROCESSING";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELED = "CANCELED"; // Mantener ortografía actual
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED"; // Nueva variante
    public static final String ORDER_STATUS_RETURNED = "RETURNED";

    // Estados de pagos
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    // Estados de productos
    public static final String PRODUCT_STATUS_ACTIVE = "ACTIVE";
    public static final String PRODUCT_STATUS_INACTIVE = "INACTIVE";
    public static final String PRODUCT_STATUS_DISCONTINUED = "DISCONTINUED";

    // ========================================================================
    // MENSAJES DE VALIDACIÓN (Mantener en inglés para compatibilidad)
    // ========================================================================

    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email format is invalid";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters";
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String LAST_NAME_REQUIRED = "Last name is required";

    // ========================================================================
    // MENSAJES DE RESPUESTA (Nuevos - en español para UI)
    // ========================================================================

    // Mensajes de éxito
    public static final String SUCCESS_USER_CREATED = "Usuario creado exitosamente";
    public static final String SUCCESS_USER_UPDATED = "Usuario actualizado exitosamente";
    public static final String SUCCESS_LOGIN = "Inicio de sesión exitoso";
    public static final String SUCCESS_LOGOUT = "Sesión cerrada exitosamente";
    public static final String SUCCESS_PASSWORD_CHANGED = "Contraseña cambiada exitosamente";
    public static final String SUCCESS_ADDRESS_CREATED = "Dirección creada exitosamente";
    public static final String SUCCESS_ADDRESS_UPDATED = "Dirección actualizada exitosamente";
    public static final String SUCCESS_ADDRESS_DELETED = "Dirección eliminada exitosamente";

    // Mensajes de error
    public static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERROR_USER_ALREADY_EXISTS = "El usuario ya existe";
    public static final String ERROR_INVALID_CREDENTIALS = "Credenciales inválidas";
    public static final String ERROR_ACCESS_DENIED = "Acceso denegado";
    public static final String ERROR_TOKEN_EXPIRED = "Token expirado";
    public static final String ERROR_TOKEN_INVALID = "Token inválido";
    public static final String ERROR_ADDRESS_NOT_FOUND = "Dirección no encontrada";
    public static final String ERROR_ADDRESS_LIMIT_EXCEEDED = "Se ha excedido el límite de direcciones";

    // ========================================================================
    // CONFIGURACIÓN
    // ========================================================================

    // Configuración de JWT
    public static final long JWT_DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L; // 24 horas
    public static final long JWT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 días
    public static final long JWT_REMEMBER_ME_EXPIRATION = 30L * 24 * 60 * 60 * 1000L; // 30 días

    // Configuración de seguridad
    public static final int BCRYPT_STRENGTH = 12;
    public static final long CORS_MAX_AGE = 3600L;

    // ========================================================================
    // PATRONES REGEX
    // ========================================================================

    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";
    public static final String REGEX_PHONE = "^[+]?[0-9\\s()-]{7,20}$";
    public static final String REGEX_NAME = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s-]+$";
    public static final String REGEX_POSTAL_CODE = "^[0-9A-Za-z\\s-]+$";

    // ========================================================================
    // CONSTRUCTOR PRIVADO
    // ========================================================================

    private Constants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }
}