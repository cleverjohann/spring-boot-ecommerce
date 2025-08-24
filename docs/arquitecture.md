```
src/main/java/com/example/springbootecommerce/
├── SpringBootEcommerceApplication.java # Punto de entrada de la aplicación Spring Boot
├── auth/ # Módulo de Autenticación y Autorización
│   ├── controller/ # Controladores para la API de autenticación
│   │   └── AuthController.java # Expone los endpoints para registro, login, etc.
│   ├── dto/ # DTOs para las solicitudes y respuestas de autenticación
│   │   ├── ChangePasswordDTO.java # DTO para cambiar la contraseña
│   │   ├── JwtResponseDTO.java # DTO para la respuesta con el token JWT
│   │   ├── LoginRequestDTO.java # DTO para la solicitud de login
│   │   ├── PasswordResetDTO.java # DTO para resetear la contraseña
│   │   ├── PasswordResetRequestDTO.java # DTO para solicitar el reseteo de contraseña
│   │   ├── RefreshTokenRequestDTO.java # DTO para solicitar un nuevo token de acceso
│   │   └── RegisterRequestDTO.java # DTO para el registro de nuevos usuarios
│   ├── entity/ # Entidades de base de datos para la autenticación
│   │   └── TokenBlacklist.java # Entidad para almacenar tokens JWT en lista negra (invalidos)
│   ├── repository/ # Repositorios para el acceso a datos de autenticación
│   │   └── TokenBlacklistRepository.java # Repositorio para la entidad TokenBlacklist
│   ├── service/ # Lógica de negocio para la autenticación
│   │   ├── AuthService.java # Interfaz para el servicio de autenticación
│   │   ├── TokenBlacklistService.java # Interfaz para el servicio de la lista negra de tokens
│   │   └── impl/ # Implementaciones de los servicios de autenticación
│   └── task/ # Tareas programadas relacionadas con la autenticación
│       └── TokenBlacklistCleanupTask.java # Tarea para limpiar tokens expirados de la lista negra
├── cart/ # Módulo del Carrito de Compras
│   ├── controller/ # Controladores para la API del carrito
│   │   └── CartController.java # Endpoints para gestionar el carrito
│   ├── dto/ # DTOs para el carrito
│   │   ├── AddItemDTO.java # DTO para añadir un item al carrito
│   │   ├── CartDTO.java # DTO que representa el carrito
│   │   ├── CartItemDTO.java # DTO que representa un item del carrito
│   │   ├── UpdateItemDTO.java # DTO para actualizar un item del carrito
│   │   └── UpdateItemQuantityDTO.java # DTO para actualizar la cantidad de un item
│   ├── entity/ # Entidades de base de datos para el carrito
│   │   ├── Cart.java # Entidad que representa el carrito de un usuario
│   │   └── CartItem.java # Entidad que representa un item dentro de un carrito
│   ├── mapper/ # Mappers para convertir entre entidades y DTOs del carrito
│   │   ├── CartItemMapper.java # Mapper para CartItem
│   │   └── CartMapper.java # Mapper para Cart
│   ├── repository/ # Repositorios para el acceso a datos del carrito
│   │   ├── CartItemRepository.java # Repositorio para CartItem
│   │   └── CartRepository.java # Repositorio para Cart
│   └── service/ # Lógica de negocio para el carrito
│       ├── CartManager.java # Interfaz para gestionar la lógica del carrito
│       ├── CartService.java # Interfaz para el servicio del carrito
│       └── impl/ # Implementaciones de los servicios del carrito
├── config/ # Configuraciones generales de la aplicación
│   ├── AsyncConfig.java # Configuración para la ejecución asíncrona
│   ├── DataBaseConfig.java # Configuración de la base de datos
│   ├── OpenApiConfig.java # Configuración de OpenAPI/Swagger
│   ├── PasswordEncoderConfig.java # Configuración del codificador de contraseñas
│   └── SecurityConfig.java # Configuración de Spring Security
├── inventory/ # Módulo de Inventario
│   ├── dto/ # DTOs para el inventario
│   │   └── StockUpdateDTO.java # DTO para actualizar el stock de un producto
│   └── service/ # Lógica de negocio para el inventario
│       ├── InventoryService.java # Interfaz para el servicio de inventario
│       ├── StockManager.java # Interfaz para gestionar el stock
│       └── impl/ # Implementaciones de los servicios de inventario
├── notification/ # Módulo de Notificaciones
│   ├── dto/ # DTOs para notificaciones
│   │   ├── EmailDTO.java # DTO para enviar un email
│   │   └── NotificationEventDTO.java # DTO para eventos de notificación
│   └── service/ # Lógica de negocio para notificaciones
│       ├── EmailService.java # Interfaz para el servicio de email
│       └── impl/ # Implementaciones de los servicios de notificación
├── order/ # Módulo de Pedidos
│   ├── controller/ # Controladores para la API de pedidos
│   │   └── OrderController.java # Endpoints para gestionar pedidos
│   ├── dto/ # DTOs para pedidos
│   │   ├── CreateGuestOrderDTO.java # DTO para crear un pedido de invitado
│   │   ├── CreateOrderDTO.java # DTO para crear un pedido
│   │   ├── GuestCartItemDTO.java # DTO para un item de carrito de invitado
│   │   ├── GuestShippingAddressDTO.java # DTO para la dirección de envío de invitado
│   │   ├── OrderDTO.java # DTO que representa un pedido
│   │   ├── OrderItemDTO.java # DTO que representa un item de un pedido
│   │   ├── OrderSummaryDTO.java # DTO con el resumen de un pedido
│   │   └── UpdateOrderStatusDTO.java # DTO para actualizar el estado de un pedido
│   ├── entity/ # Entidades de base de datos para pedidos
│   │   ├── Order.java # Entidad que representa un pedido
│   │   └── OrderItem.java # Entidad que representa un item de un pedido
│   ├── mapper/ # Mappers para convertir entre entidades y DTOs de pedidos
│   │   ├── OrderItemMapper.java # Mapper para OrderItem
│   │   └── OrderMapper.java # Mapper para Order
│   ├── repository/ # Repositorios para el acceso a datos de pedidos
│   │   ├── OrderItemService.java # Servicio para OrderItem
│   │   ├── OrderRepository.java # Repositorio para Order
│   │   └── specification/ # Especificaciones para consultas de pedidos
│   └── service/ # Lógica de negocio para pedidos
│       ├── OrderService.java # Interfaz para el servicio de pedidos
│       └── impl/ # Implementaciones de los servicios de pedidos
├── payment/ # Módulo de Pagos
│   ├── controller/ # Controladores para la API de pagos
│   │   └── PaymentController.java # Endpoints para procesar pagos
│   ├── dto/ # DTOs para pagos
│   │   ├── PaymentDTO.java # DTO que representa un pago
│   │   ├── PaymentIntentDTO.java # DTO para la intención de pago
│   │   └── WebhookEventDTO.java # DTO para eventos de webhook
│   ├── entity/ # Entidades de base de datos para pagos
│   │   └── Payment.java # Entidad que representa un pago
│   ├── exception/ # Excepciones personalizadas para pagos
│   │   └── PaymentException.java # Excepción para errores de pago
│   ├── mapper/ # Mappers para convertir entre entidades y DTOs de pagos
│   │   └── PaymentMapper.java # Mapper para Payment
│   ├── repository/ # Repositorios para el acceso a datos de pagos
│   │   └── PaymentRepository.java # Repositorio para Payment
│   └── service/ # Lógica de negocio para pagos
│       ├── PaymentGateway.java # Interfaz para la pasarela de pagos
│       ├── PaymentService.java # Interfaz para el servicio de pagos
│       └── impl/ # Implementaciones de los servicios de pagos
├── product/ # Módulo de Productos
│   ├── controller/ # Controladores para la API de productos
│   │   ├── CategoriaController.java # Endpoints para gestionar categorías
│   │   └── ProductoController.java # Endpoints para gestionar productos
│   ├── dto/ # DTOs para productos
│   │   ├── CategoriaDTO.java # DTO que representa una categoría
│   │   ├── CategoriaTreeDTO.java # DTO para la estructura de árbol de categorías
│   │   ├── CreateCategoriaDTO.java # DTO para crear una categoría
│   │   ├── CreateProductoDTO.java # DTO para crear un producto
│   │   ├── CreateReviewDTO.java # DTO para crear una reseña
│   │   ├── ProductoDTO.java # DTO que representa un producto
│   │   ├── ProductoSearchCriteria.java # Criterios de búsqueda para productos
│   │   ├── ProductoSummaryDTO.java # DTO con el resumen de un producto
│   │   ├── ReviewDTO.java # DTO que representa una reseña
│   │   └── UpdateProductoDTO.java # DTO para actualizar un producto
│   ├── entity/ # Entidades de base de datos para productos
│   │   ├── Categoria.java # Entidad que representa una categoría de producto
│   │   ├── Producto.java # Entidad que representa un producto
│   │   └── Review.java # Entidad que representa una reseña de producto
│   ├── mapper/ # Mappers para convertir entre entidades y DTOs de productos
│   │   ├── CategoriaMapper.java # Mapper para Categoria
│   │   ├── ProductoMapper.java # Mapper para Producto
│   │   └── ReviewMapper.java # Mapper para Review
│   ├── repository/ # Repositorios para el acceso a datos de productos
│   │   ├── CategoriaRepository.java # Repositorio para Categoria
│   │   ├── ProductoRepository.java # Repositorio para Producto
│   │   ├── ReviewRepository.java # Repositorio para Review
│   │   └── specification/ # Especificaciones para consultas de productos
│   └── service/ # Lógica de negocio para productos
│       ├── CategoriaService.java # Interfaz para el servicio de categorías
│       ├── ProductoService.java # Interfaz para el servicio de productos
│       └── impl/ # Implementaciones de los servicios de productos
├── shared/ # Componentes compartidos a través de la aplicación
│   ├── audit/ # Clases para la auditoría de entidades
│   │   ├── Auditable.java # Interfaz para entidades auditables
│   │   ├── AuditConfig.java # Configuración de la auditoría
│   │   └── AuditorAwareImpl.java # Implementación para obtener el auditor actual
│   ├── dto/ # DTOs compartidos
│   │   ├── ApiResponse.java # DTO para respuestas de API genéricas
│   │   ├── ErrorResponse.java # DTO para respuestas de error
│   │   └── PageResponse.java # DTO para respuestas paginadas
│   ├── exception/ # Excepciones personalizadas y manejo global
│   │   ├── BusinessException.java # Excepción para errores de negocio
│   │   ├── CustomAccessDeniedHandler.java # Manejador para acceso denegado
│   │   ├── DuplicateResourceException.java # Excepción para recursos duplicados
│   │   ├── GlobalExceptionHandler.java # Manejador de excepciones global
│   │   ├── InsufficientStockException.java # Excepción para stock insuficiente
│   │   ├── PaymentException.java # Excepción para errores de pago
│   │   ├── ResourceNotFoundException.java # Excepción para recursos no encontrados
│   │   └── UnauthorizedOperationException.java # Excepción para operaciones no autorizadas
│   ├── mapper/ # Mappers base
│   │   └── BaseMapper.java # Interfaz base para mappers
│   ├── security/ # Componentes de seguridad compartidos
│   │   ├── CustomUserDetailService.java # Servicio para obtener detalles de usuario
│   │   ├── JwtAuthenticationEntryPoint.java # Punto de entrada para la autenticación JWT
│   │   ├── JwtRequestFilter.java # Filtro para peticiones JWT
│   │   ├── JwtResponseFilter.java # Filtro para respuestas JWT
│   │   └── JwtService.java # Servicio para la gestión de JWT
│   └── util/ # Clases de utilidad
│       ├── Constants.java # Constantes de la aplicación
│       └── ValidationUtils.java # Utilidades de validación
└── user/ # Módulo de Usuarios
    ├── controller/ # Controladores para la API de usuarios
    │   └── UserController.java # Endpoints para gestionar usuarios
    ├── dto/ # DTOs para usuarios
    │   ├── AddressDTO.java # DTO que representa una dirección
    │   ├── CreatedAddressDTO.java # DTO para la creación de direcciones
    │   ├── UpdateUserDTO.java # DTO para actualizar un usuario
    │   └── UserDTO.java # DTO que representa un usuario
    ├── entity/ # Entidades de base de datos para usuarios
    │   ├── Address.java # Entidad que representa una dirección
    │   ├── Role.java # Entidad que representa un rol de usuario
    │   └── User.java # Entidad que representa un usuario
    ├── mapper/ # Mappers para convertir entre entidades y DTOs de usuarios
    │   ├── AddressMapper.java # Mapper para Address
    │   └── UserMapper.java # Mapper para User
    ├── repository/ # Repositorios para el acceso a datos de usuarios
    │   ├── AddressRepository.java # Repositorio para Address
    │   ├── RoleRepository.java # Repositorio para Role
    │   ├── UserRepository.java # Repositorio para User
    │   ├── UserStatisticsProjection.java # Proyección para estadísticas de usuario
    │   └── specification/ # Especificaciones para consultas de usuarios
    ├── service/ # Lógica de negocio para usuarios
    │   ├── AddressValidator.java # Validador de direcciones
    │   ├── UserService.java # Interfaz para el servicio de usuarios
    │   └── impl/ # Implementaciones de los servicios de usuarios
    └── util/ # Utilidades específicas del módulo de usuarios
        └── RoleUtils.java # Utilidades para la gestión de roles

src/main/resources/
├── application.yml # Configuración principal de la aplicación
├── application-dev.yml # Configuración para el entorno de desarrollo
├── application-prod.yml # Configuración para el entorno de producción
├── application-qa.yml # Configuración para el entorno de QA
├── db/migration/ # Scripts de migración de base de datos (Flyway)
│   ├── V1__Initial_Schema.sql # Script inicial de la base de datos
│   ├── V2__Sample_Data.sql # Script con datos de ejemplo
│   ├── V3__Add_Optional_Columns_To_Addresses.sql # Script para añadir columnas opcionales a las direcciones
│   └── V4__Create_Token_Blacklist_Table.sql # Script para crear la tabla de la lista negra de tokens
├── static/ # Archivos estáticos (CSS, JS, imágenes)
└── templates/ # Plantillas de correo electrónico (Thymeleaf)
    ├── order-confirmation.html # Plantilla para la confirmación de pedido
    └── welcome-email.html # Plantilla para el email de bienvenida

src/test/java/com/example/springbootecommerce/
├── SpringBootEcommerceApplicationTests.java # Clase principal de tests
├── auth/ # Tests para el módulo de autenticación
│   └── controller/ # Tests para los controladores de autenticación
│       └── AuthControllerTest.java # Tests para AuthController
├── product/ # Tests para el módulo de productos
│   └── controller/ # Tests para los controladores de productos
│       ├── CategoriaControllerTest.java # Tests para CategoriaController
│       └── ProductoControllerTest.java # Tests para ProductoController
└── user/ # Tests para el módulo de usuarios
    └── controller/ # Tests para los controladores de usuarios
        └── UserControllerTest.java # Tests para UserController
```