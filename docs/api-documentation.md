# Documentación de la API

Este documento proporciona una descripción general de los puntos finales de la API disponibles en el proyecto de E-commerce.

## API de Autenticación (`/api/v1/auth`)

| Método | Ruta                               | Descripción                                  |
|--------|------------------------------------|----------------------------------------------|
| POST   | `/api/v1/auth/login`               | Autentica a un usuario y devuelve tokens JWT. |
| POST   | `/api/v1/auth/register`            | Registra un nuevo usuario.                   |
| POST   | `/api/v1/auth/refresh`             | Refresca un token de acceso.                 |
| POST   | `/api/v1/auth/logout`              | Cierra la sesión del usuario actual.         |
| PUT    | `/api/v1/auth/change-password`     | Cambia la contraseña del usuario actual.     |
| POST   | `/api/v1/auth/forgot-password`     | Envía un correo electrónico de restablecimiento de contraseña.|
| POST   | `/api/v1/auth/reset-password`      | Restablece la contraseña con un token válido. |
| GET    | `/api/v1/auth/validate-token`      | Valida un token JWT.                         |
| GET    | `/api/v1/auth/check-email`         | Comprueba si un correo electrónico está disponible.       |
| GET    | `/api/v1/auth/me`                  | Obtiene la información del usuario actual.   |

## API de Carrito (`/api/v1/cart`)

| Método  | Ruta                          | Descripción                                |
|---------|-------------------------------|--------------------------------------------|
| GET     | `/api/v1/cart`                | Obtiene el carrito del usuario actual.     |
| GET     | `/api/v1/cart/validate-stock` | Valida el stock de los artículos en el carrito.|
| POST    | `/api/v1/cart/items`          | Agrega un artículo al carrito.             |
| PUT     | `/api/v1/cart/items/{itemId}` | Actualiza la cantidad de un artículo en el carrito.|
| DELETE  | `/api/v1/cart/items/{itemId}` | Elimina un artículo del carrito.           |
| DELETE  | `/api/v1/cart/clear`          | Vacía el carrito.                          |

## API de Pedidos (`/api/v1/orders`)

| Método | Ruta                                      | Descripción                                       |
|--------|-------------------------------------------|---------------------------------------------------|
| POST   | `/api/v1/orders`                          | Crea un nuevo pedido para un usuario autenticado. |
| GET    | `/api/v1/orders`                          | Obtiene todos los pedidos del usuario autenticado.|
| GET    | `/api/v1/orders/{orderId}`                | Obtiene un pedido específico del usuario autenticado.|
| GET    | `/api/v1/orders/by-status/{status}`       | Obtiene los pedidos del usuario autenticado por estado.|
| GET    | `/api/v1/orders/{orderId}/status-history` | Obtiene el historial de estado de un pedido.      |
| POST   | `/api/v1/orders/guest`                    | Crea un nuevo pedido para un usuario invitado.    |
| GET    | `/api/v1/orders/guest/search`             | Busca pedidos de invitados por correo electrónico y rango de fechas.|
| GET    | `/api/v1/orders/admin/search/guest`       | Busca pedidos de invitados por correo electrónico (solo administradores).|
| GET    | `/api/v1/orders/admin/all`                | Obtiene todos los pedidos del sistema (solo administradores).|
| GET    | `/api/v1/orders/admin/{orderId}`          | Obtiene un pedido específico por ID (solo administradores).|
| PUT    | `/api/v1/orders/admin/{orderId}/status`   | Actualiza el estado de un pedido (solo administradores).|
| GET    | `/api/v1/orders/admin/search/user-status` | Busca pedidos por usuario y estado (solo administradores).|
| PUT    | `/api/v1/orders/admin/{orderId}/cancel`   | Cancela un pedido (solo administradores).         |
| PUT    | `/api/v1/orders/admin/bulk/ship`          | Marca varios pedidos como enviados (solo administradores).|
| GET    | `/api/v1/orders/admin/reports/revenue`    | Obtiene un informe de ingresos (solo administradores).|
| GET    | `/api/v1/orders/admin/statistics`         | Obtiene estadísticas generales de pedidos (solo administradores).|
| GET    | `/api/v1/orders/admin/requiring-action`   | Obtiene pedidos que requieren atención (solo administradores).|
| GET    | `/api/v1/orders/admin/search`             | Búsqueda avanzada de pedidos (solo administradores).|

## API de Categorías (`/api/v1/categories`)

| Método | Ruta                               | Descripción                               |
|--------|------------------------------------|-------------------------------------------|
| GET    | `/api/v1/categories`               | Obtiene todas las categorías.             |
| GET    | `/api/v1/categories/root`          | Obtiene las categorías raíz.              |
| GET    | `/api/v1/categories/tree`          | Obtiene el árbol de categorías.           |
| GET    | `/api/v1/categories/{id}`          | Obtiene una categoría por ID.             |
| GET    | `/api/v1/categories/{id}/subcategorias` | Obtiene las subcategorías de una categoría.|
| GET    | `/api/v1/categories/search`        | Busca categorías.                         |
| POST   | `/api/v1/categories/admin`         | Crea una nueva categoría (solo administradores).|
| PUT    | `/api/v1/categories/admin/{id}`    | Actualiza una categoría (solo administradores).|
| DELETE | `/api/v1/categories/admin/{id}`    | Elimina una categoría (solo administradores).|

## API de Productos (`/api/v1/products`)

| Método | Ruta                                  | Descripción                                  |
|--------|---------------------------------------|----------------------------------------------|
| GET    | `/api/v1/products`                    | Busca productos.                             |
| GET    | `/api/v1/products/{id}`               | Obtiene un producto por ID.                  |
| GET    | `/api/v1/products/sku/{sku}`          | Obtiene un producto por SKU.                 |
| GET    | `/api/v1/products/categoria/{categoriaId}` | Obtiene productos por categoría.             |
| GET    | `/api/v1/products/{id}/relacionados`  | Obtiene productos relacionados.             |
| GET    | `/api/v1/products/mas-vendidos`       | Obtiene los productos más vendidos.         |
| GET    | `/api/v1/products/mejor-calificados`  | Obtiene los productos mejor calificados.     |
| POST   | `/api/v1/products`                    | Crea un nuevo producto (solo administradores).|
| PUT    | `/api/v1/products/{id}`               | Actualiza un producto (solo administradores).|
| DELETE | `/api/v1/products/{id}`               | Elimina un producto (solo administradores).  |
| PUT    | `/api/v1/products/{id}/stock`         | Actualiza el stock de un producto (solo administradores).|
| GET    | `/api/v1/products/stock-bajo`         | Obtiene productos con poco stock (solo administradores).|
| GET    | `/api/v1/products/restock-necesario`  | Obtiene productos que necesitan reposición (solo administradores).|

## API de Usuarios (`/api/v1/users`)

| Método | Ruta                                      | Descripción                                     |
|--------|-------------------------------------------|-------------------------------------------------|
| GET    | `/api/v1/users/me`                        | Obtiene el perfil del usuario actual.           |
| PUT    | `/api/v1/users/me`                        | Actualiza el perfil del usuario actual.         |
| POST   | `/api/v1/users/me/change-password`        | Cambia la contraseña del usuario actual.        |
| DELETE | `/api/v1/users/me`                        | Desactiva la cuenta del usuario actual.          |
| GET    | `/api/v1/users/me/addresses`              | Obtiene las direcciones del usuario actual.     |
| GET    | `/api/v1/users/me/addresses/default`      | Obtiene la dirección predeterminada del usuario actual.|
| POST   | `/api/v1/users/me/addresses`              | Agrega una nueva dirección para el usuario actual.|
| PUT    | `/api/v1/users/me/addresses/{addressId}`  | Actualiza una dirección para el usuario actual. |
| DELETE | `/api/v1/users/me/addresses/{addressId}`  | Elimina una dirección para el usuario actual.   |
| PUT    | `/api/v1/users/me/addresses/{addressId}/default` | Establece una dirección como predeterminada para el usuario actual.|
| GET    | `/api/v1/users/admin/{userId}`            | Obtiene el perfil de un usuario (solo administradores).|
| GET    | `/api/v1/users/admin/{userId}/statistics` | Obtiene un usuario con estadísticas (solo administradores).|
| GET    | `/api/v1/users/admin`                     | Obtiene todos los usuarios (solo administradores).|
| PUT    | `/api/v1/users/admin/{userId}/activate`   | Activa un usuario (solo administradores).       |
| PUT    | `/api/v1/users/admin/{userId}/deactivate` | Desactiva un usuario (solo administradores).    |
| GET    | `/api/v1/users/validate-email`            | Valida la disponibilidad de un correo electrónico.|
| GET    | `/api/v1/users/admin/recent`              | Obtiene usuarios registrados recientemente (solo administradores).|