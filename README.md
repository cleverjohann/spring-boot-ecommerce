# Spring Boot E-commerce Backend 🚀

Una plataforma de e-commerce moderna construida con Spring Boot 3, siguiendo las mejores prácticas de desarrollo y principios SOLID.

---

## ✨ Características Principales

* **Autenticación JWT Stateless**: Sistema seguro sin sesiones en el servidor.
* **Gestión de Usuarios y Roles**: Control de acceso basado en roles (RBAC).
* **Catálogo de Productos**: Incluye búsqueda avanzada y categorías jerárquicas.
* **Carrito Persistente**: El carrito de compras se mantiene entre sesiones de usuario.
* **Procesamiento de Órdenes**: Lógica transaccional para garantizar la integridad histórica de los pedidos.
* **Integración de Pagos**: Soporte para múltiples gateways de pago.
* **Notificaciones por Email**: Sistema asíncrono para el envío de notificaciones.
* **API REST Documentada**: Documentación interactiva con OpenAPI/Swagger.
* **Logging Estructurado**: Logs en formato JSON para un fácil análisis y monitoreo.
* **Validación Robusta**: Uso de Bean Validation para asegurar la integridad de los datos.
* **Despliegue Dockerizado**: Contenedores listos para un entorno de producción.

---

## 🛠️ Stack Tecnológico

### Core
* **Java 17**: Lenguaje principal.
* **Spring Boot 3.2**: Framework principal.
* **Spring Security**: Para autenticación y autorización.
* **Spring Data JPA**: Capa de persistencia de datos.
* **PostgreSQL**: Base de datos principal para producción.

### Herramientas y Librerías
* **MapStruct**: Mapeo automático y eficiente entre DTOs y entidades.
* **Lombok**: Reducción de código repetitivo (boilerplate).
* **Flyway**: Para la migración y versionado de la base de datos.
* **JJWT**: Creación y validación de tokens JWT.
* **OpenAPI 3**: Documentación de la API.
* **Testcontainers**: Para pruebas de integración.
* **Docker**: Para la contenedorización de la aplicación.

---

## 🏗️ Arquitectura

### Principios de Diseño
* **Feature-based packaging**: El código se organiza por funcionalidades de negocio.
* **Principios SOLID**: Para un código más mantenible, escalable y robusto.
* **Clean Architecture**: Separación clara de responsabilidades en capas.
* **Domain-Driven Design (DDD)**: El modelado del software se centra en el dominio del negocio.

### Estructura del Proyecto
El proyecto está organizado por módulos para una mejor separación de responsabilidades.

```
src/main/java/com/ecommerce/
├── shared/         # Componentes compartidos
│   ├── exception/  # Excepciones globales
│   ├── security/   # Configuración de seguridad
│   ├── dto/        # DTOs comunes
│   └── util/       # Utilidades
├── user/           # Módulo de usuarios
├── auth/           # Módulo de autenticación
├── product/        # Módulo de productos
├── cart/           # Módulo de carrito
├── order/          # Módulo de órdenes
├── payment/        # Módulo de pagos
└── notification/   # Módulo de notificaciones
```

---

## 🚀 Inicio Rápido

### Prerrequisitos
* Java 17+
* Maven 3.8+
* Docker y Docker Compose (Opcional)

### 1. Clonación del Repositorio
```bash
git clone [https://github.com/tu-usuario/spring-boot-ecommerce.git](https://github.com/tu-usuario/spring-boot-ecommerce.git)
cd spring-boot-ecommerce
```

### 2. Configuración de Base de Datos

**Opción A: Usar H2 (Desarrollo)**
No se requiere configuración adicional. La aplicación utiliza el perfil `dev` por defecto.

**Opción B: Usar PostgreSQL con Docker**
```bash
# Levantar el contenedor de PostgreSQL
docker-compose up -d postgres-db

# Opcional: Levantar pgAdmin para administrar la base de datos
docker-compose up -d pgadmin
```
* **Acceso a pgAdmin**: `http://localhost:8080`
* **Email**: `admin@ecommerce.com` / **Contraseña**: `admin123`

### 3. Ejecutar la Aplicación
```bash
# Compilar y ejecutar con el perfil por defecto (dev)
mvn spring-boot:run

# O especificar un perfil
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Acceder a la Aplicación
* **API Base**: `http://localhost:8080/api/v1`
* **Swagger UI**: `http://localhost:8080/swagger-ui.html`
* **H2 Console** (solo perfil `dev`): `http://localhost:8080/h2-console`
    * **JDBC URL**: `jdbc:h2:mem:ecommerce_dev`
    * **Usuario**: `sa` / **Contraseña**: (vacía)

---

## 🧪 Testing

```bash
# Ejecutar todas las pruebas (unitarias y de integración)
mvn test

# Ejecutar solo pruebas unitarias
mvn test -Dtest="**/*Test"

# Ejecutar solo pruebas de integración
mvn test -Dtest="**/*IT"

# Generar reporte de cobertura de código con JaCoCo
mvn jacoco:report
```

---

## 🔌 Usando la API

### 1. Registro de Usuario
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "password123"
}'
```

### 2. Inicio de Sesión
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{
  "email": "juan@example.com",
  "password": "password123"
}'
```

### 3. Obtener Productos (Público)
```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10"
```

### 4. Agregar al Carrito (Autenticado)
```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "productId": 1,
  "quantity": 2
}'
```

---

## 🧑‍🤝‍🧑 Usuarios de Prueba
La base de datos se precarga con los siguientes usuarios para facilitar las pruebas.

| Email                     | Contraseña  | Rol       | Descripción           |
| ------------------------- | ----------- | --------- | --------------------- |
| `admin@ecommerce.com`     | `password123` | `ADMIN`   | Administrador completo|
| `user@ecommerce.com`      | `password123` | `USER`    | Usuario estándar      |
| `moderator@ecommerce.com` | `password123` | `MODERATOR` | Moderador             |
| `customer@ecommerce.com`  | `password123` | `USER`    | Cliente de prueba     |

---

## ⚙️ Configuración

### Perfiles de Entorno
* `dev`: Perfil de desarrollo local que utiliza la base de datos H2.
* `qa`: Ambiente de pruebas configurado para usar PostgreSQL.
* `prod`: Perfil de producción que obtiene su configuración de variables de entorno.

### Variables de Entorno (`prod`)
```env
# Base de Datos
DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce_prod
DATABASE_USERNAME=ecommerce_user
DATABASE_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000 # 24 horas en milisegundos

# Email (SMTP)
EMAIL_HOST=smtp.gmail.com
EMAIL_USERNAME=noreply@ecommerce.com
EMAIL_PASSWORD=app_password

# Opcional: Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

---

## 🐳 Docker

### Construir Imagen
```bash
# Empaquetar la aplicación, omitiendo las pruebas
mvn clean package -DskipTests

# Construir la imagen Docker
docker build -t ecommerce-backend .
```

### Ejecutar con Docker Compose
```bash
# Levantar toda la infraestructura (App + DB + pgAdmin)
docker-compose up -d

# Levantar solo la aplicación (requiere una BD externa ya configurada)
docker-compose up app
```

---

## 📈 Monitoreo y Observabilidad

### Actuator Endpoints
* `/actuator/health`: Muestra el estado de salud de la aplicación.
* `/actuator/metrics`: Expone métricas de la JVM, peticiones HTTP, etc.
* `/actuator/info`: Muestra información general de la aplicación.

### Logs
* **Formato**: JSON estructurado para facilitar el análisis.
* **Nivel por defecto**: `INFO`.
* **Correlación**: Se incluye un `Trace ID` en cada request para seguir el flujo de una petición.

---

## 🤝 Contribución

1.  **Fork** del proyecto.
2.  Crea una nueva rama para tu feature (`git checkout -b feature/nueva-funcionalidad`).
3.  Haz **commit** de tus cambios (`git commit -am 'Agregar nueva funcionalidad'`).
4.  **Push** a tu rama (`git push origin feature/nueva-funcionalidad`).
5.  Crea un nuevo **Pull Request**.

### Convenciones de Código
* Seguir las convenciones de código de Java.
* Usar Lombok para reducir código repetitivo.
* Documentar todas las APIs públicas con OpenAPI.
* Escribir pruebas para toda nueva funcionalidad.
* Mantener una cobertura de pruebas superior al 80%.

---

## 🗺️ Roadmap

- [ ] **v1.1**: Búsqueda avanzada con Elasticsearch.
- [ ] **v1.2**: Sistema de cupones y descuentos.
- [ ] **v1.3**: Implementación de Wishlist de productos.
- [ ] **v1.4**: Sistema de recomendaciones de productos.
- [ ] **v1.5**: Exponer una API con GraphQL.
- [ ] **v2.0**: Migración a una arquitectura de microservicios.

---

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

## 📬 Contacto

* **Desarrollador**: CleverJohann
* **Email**: mcleverjohann@gmail.com
* **GitHub**: [@cleverjohann](https://github.com/cleverjohann)

¡Si este proyecto te fue útil, considera darle una estrella! ⭐
