```
src/main/java/com/ecommerce/
├── EcommerceApplication.java
├── config/                          # Configuraciones generales
│   ├── SecurityConfig.java
│   ├── DatabaseConfig.java
│   ├── AsyncConfig.java
│   └── OpenApiConfig.java
├── shared/                          # Componentes compartidos
│   ├── exception/                   # Excepciones globales
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── InsufficientStockException.java
│   │   └── BusinessException.java
│   ├── dto/                        # DTOs compartidos
│   │   ├── ApiResponse.java
│   │   ├── PageResponse.java
│   │   └── ErrorResponse.java
│   ├── mapper/                     # Mappers base
│   │   └── BaseMapper.java
│   ├── security/                   # Componentes de seguridad
│   │   ├── JwtService.java
│   │   ├── JwtRequestFilter.java
│   │   └── CustomUserDetailsService.java
│   ├── audit/                      # Auditoría
│   │   ├── AuditConfig.java
│   │   └── AuditorAwareImpl.java
│   └── util/                       # Utilidades
│       ├── Constants.java
│       └── ValidationUtils.java
├── user/                           # Módulo de Usuario
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── impl/
│   │       └── UserServiceImpl.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── UserStatisticsProjection.java  
│   ├── specification/                 
│   │   └── UserSpecification.java     
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── Address.java
│   ├── dto/
│   │   ├── UserDTO.java
│   │   ├── UpdateUserDTO.java
│   │   ├── AddressDTO.java
│   │   └── CreateAddressDTO.java
│   └── mapper/
│       ├── UserMapper.java
│       └── AddressMapper.java
│
├── auth/                          # Módulo de Autenticación
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── impl/
│   │       └── AuthServiceImpl.java
│   └── dto/
│       ├── RegisterRequestDTO.java
│       ├── LoginRequestDTO.java
│       └── JwtResponseDTO.java
├── product/                       # Módulo de Productos
│   ├── controller/
│   │   ├── ProductController.java
│   │   └── CategoryController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── CategoryService.java
│   │   └── impl/
│   │       ├── ProductServiceImpl.java
│   │       └── CategoryServiceImpl.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── CategoryRepository.java
│   │   └── specification/
│   │       └── ProductSpecification.java
│   ├── entity/
│   │   ├── Product.java
│   │   ├── Category.java
│   │   └── Review.java
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   ├── CreateProductDTO.java
│   │   ├── UpdateProductDTO.java
│   │   ├── CategoryDTO.java
│   │   ├── CreateCategoryDTO.java
│   │   └── ReviewDTO.java
│   └── mapper/
│       ├── ProductMapper.java
│       ├── CategoryMapper.java
│       └── ReviewMapper.java
├── cart/                          # Módulo de Carrito
│   ├── controller/
│   │   └── CartController.java
│   ├── service/
│   │   ├── CartService.java
│   │   └── impl/
│   │       └── CartServiceImpl.java
│   ├── repository/
│   │   ├── CartRepository.java
│   │   └── CartItemRepository.java
│   ├── entity/
│   │   ├── Cart.java
│   │   └── CartItem.java
│   ├── dto/
│   │   ├── CartDTO.java
│   │   ├── CartItemDTO.java
│   │   ├── AddItemDTO.java
│   │   └── UpdateItemQuantityDTO.java
│   └── mapper/
│       ├── CartMapper.java
│       └── CartItemMapper.java
├── order/                         # Módulo de Órdenes
│   ├── controller/
│   │   └── OrderController.java
│   ├── service/
│   │   ├── OrderService.java
│   │   └── impl/
│   │       └── OrderServiceImpl.java
│   ├── repository/
│   │   ├── OrderRepository.java
│   │   └── OrderItemRepository.java
│   ├── entity/
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── dto/
│   │   ├── OrderDTO.java
│   │   ├── OrderSummaryDTO.java
│   │   ├── OrderItemDTO.java
│   │   └── UpdateOrderStatusDTO.java
│   └── mapper/
│       ├── OrderMapper.java
│       └── OrderItemMapper.java
├── inventory/                     # Módulo de Inventario
│   ├── service/
│   │   ├── InventoryService.java
│   │   └── impl/
│   │       └── InventoryServiceImpl.java
│   └── dto/
│       └── StockUpdateDTO.java
├── payment/                       # Módulo de Pagos
│   ├── controller/
│   │   └── PaymentController.java
│   ├── service/
│   │   ├── PaymentService.java
│   │   ├── PaymentGateway.java
│   │   └── impl/
│   │       ├── PaymentServiceImpl.java
│   │       └── StripePaymentGateway.java
│   ├── repository/
│   │   └── PaymentRepository.java
│   ├── entity/
│   │   └── Payment.java
│   ├── dto/
│   │   ├── PaymentDTO.java
│   │   ├── PaymentIntentDTO.java
│   │   └── WebhookEventDTO.java
│   └── mapper/
│       └── PaymentMapper.java
└── notification/                  # Módulo de Notificaciones
    ├── service/
    │   ├── EmailService.java
    │   └── impl/
    │       └── EmailServiceImpl.java
    └── dto/
        ├── EmailDTO.java
        └── NotificationEventDTO.java

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-qa.yml
├── application-prod.yml
├── db/migration/                  # Scripts de Flyway
│   ├── V1__Initial_Schema.sql
│   ├── V2__Add_Indexes.sql
│   └── V3__Sample_Data.sql
└── templates/                     # Plantillas de email
    ├── order-confirmation.html
    └── welcome-email.html

src/test/java/com/ecommerce/
├── integration/                   # Pruebas de integración
├── unit/                         # Pruebas unitarias
└── testcontainers/               # Configuración Testcontainers
```