-- V1__Initial_Schema.sql
-- Migración inicial para crear todas las tablas del sistema E-commerce
-- Flyway ejecutará este script automáticamente al arrancar la aplicación

-- ==============================================================================
-- TABLA: users
-- Almacena la información básica de los usuarios del sistema
-- ==============================================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_by VARCHAR(100),
                       updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: roles
-- Define los roles disponibles en el sistema (USER, ADMIN, etc.)
-- ==============================================================================
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(200),
                       created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_by VARCHAR(100),
                       updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: user_roles
-- Tabla de unión para la relación muchos a muchos entre users y roles
-- ==============================================================================
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            assigned_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                            PRIMARY KEY (user_id, role_id)
);

-- ==============================================================================
-- TABLA: addresses
-- Direcciones de los usuarios para facturación y envío
-- ==============================================================================
CREATE TABLE addresses (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           street VARCHAR(255) NOT NULL,
                           city VARCHAR(100) NOT NULL,
                           state VARCHAR(100) NOT NULL,
                           postal_code VARCHAR(20) NOT NULL,
                           country VARCHAR(100) NOT NULL,
                           is_default BOOLEAN NOT NULL DEFAULT false,
                           created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_by VARCHAR(100),
                           updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: categories
-- Categorías de productos con soporte para jerarquías (subcategorías)
-- ==============================================================================
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
                            is_active BOOLEAN NOT NULL DEFAULT true,
                            display_order INT DEFAULT 0,
                            created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_by VARCHAR(100),
                            updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: products
-- Catálogo de productos del e-commerce
-- ==============================================================================
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                          sku VARCHAR(100) NOT NULL UNIQUE,
                          stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                          category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
                          image_url VARCHAR(500),
                          is_active BOOLEAN NOT NULL DEFAULT true,
                          weight DECIMAL(8,3) CHECK (weight >= 0),
                          dimensions VARCHAR(100), -- formato: "LxWxH cm"
                          brand VARCHAR(100),
                          created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_by VARCHAR(100),
                          updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: reviews
-- Reseñas y calificaciones de productos por parte de los usuarios
-- ==============================================================================
CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                         user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         title VARCHAR(200),
                         comment TEXT,
                         is_verified_purchase BOOLEAN NOT NULL DEFAULT false,
                         created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_by VARCHAR(100),
                         updated_at TIMESTAMP,

    -- Un usuario solo puede hacer una reseña por producto
                         UNIQUE(product_id, user_id)
);

-- ==============================================================================
-- TABLA: carts
-- Carritos de compras de los usuarios
-- ==============================================================================
CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
                       created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_by VARCHAR(100),
                       updated_at TIMESTAMP
);

-- ==============================================================================
-- TABLA: cart_items
-- Items individuales dentro de cada carrito
-- ==============================================================================
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                            product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                            quantity INT NOT NULL CHECK (quantity > 0),
                            added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Un producto solo puede estar una vez en un carrito
                            UNIQUE(cart_id, product_id)
);

-- ==============================================================================
-- TABLA: orders
-- Órdenes de compra realizadas por los usuarios
-- ==============================================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,

    -- Campos para guest checkout
                        guest_email VARCHAR(255),
                        guest_first_name VARCHAR(100),
                        guest_last_name VARCHAR(100),

    -- Total calculado al momento de la orden (desnormalizado para performance)
                        total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),

    -- Status de la orden
                        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Información de envío (snapshot al momento de la orden)
                        shipping_street VARCHAR(255) NOT NULL,
                        shipping_city VARCHAR(100) NOT NULL,
                        shipping_state VARCHAR(100) NOT NULL,
                        shipping_postal_code VARCHAR(20) NOT NULL,
                        shipping_country VARCHAR(100) NOT NULL,

    -- Notas especiales del cliente
                        notes TEXT,

                        order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        shipped_date TIMESTAMP,
                        delivered_date TIMESTAMP,

                        created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_by VARCHAR(100),
                        updated_at TIMESTAMP,

    -- Constraint: debe tener usuario O información de guest
                        CHECK (
                            (user_id IS NOT NULL AND guest_email IS NULL) OR
                            (user_id IS NULL AND guest_email IS NOT NULL)
                            )
);

-- ==============================================================================
-- TABLA: order_items
-- Items individuales de cada orden (snapshot de productos al momento de compra)
-- ==============================================================================
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,

    -- Snapshot de información del producto al momento de la compra
                             product_name VARCHAR(255) NOT NULL,
                             product_sku VARCHAR(100) NOT NULL,
                             price_at_purchase DECIMAL(10,2) NOT NULL CHECK (price_at_purchase >= 0),

                             quantity INT NOT NULL CHECK (quantity > 0),
                             subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0)
);

-- ==============================================================================
-- TABLA: payments
-- Registros de pagos asociados a las órdenes
-- ==============================================================================
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Información del pago
                          payment_method VARCHAR(50) NOT NULL, -- CREDIT_CARD, PAYPAL, STRIPE, etc.
                          payment_gateway VARCHAR(50) NOT NULL, -- stripe, paypal, mercadopago, etc.
                          transaction_id VARCHAR(255), -- ID del proveedor de pago
                          gateway_payment_id VARCHAR(255), -- ID interno del gateway

                          amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
                          currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                          status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Timestamps importantes
                          payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          processed_date TIMESTAMP,

    -- Información adicional del gateway
                          gateway_response JSON,
                          failure_reason TEXT,

                          created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_by VARCHAR(100),
                          updated_at TIMESTAMP
);

-- ==============================================================================
-- ÍNDICES PARA OPTIMIZACIÓN DE CONSULTAS
-- ==============================================================================

-- Índices para búsquedas frecuentes en users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);

-- Índices para user_roles (JOIN frecuente)
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Índices para addresses
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_default ON addresses(user_id, is_default);

-- Índices para categories (jerarquía)
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(is_active);

-- Índices críticos para products (tabla con más consultas)
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_name_gin ON products USING gin(to_tsvector('spanish', name));

-- Índices para reviews
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(product_id, rating);

-- Índices para carts
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Índices críticos para orders (consultas frecuentes por usuario y estado)
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_orders_guest_email ON orders(guest_email);

-- Índices para order_items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Índices para payments
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_gateway ON payments(payment_gateway);

-- ==============================================================================
-- TRIGGERS PARA FUNCIONALIDADES AUTOMÁTICAS
-- ==============================================================================

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger a todas las tablas que tienen updated_at
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_addresses_updated_at
    BEFORE UPDATE ON addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_carts_updated_at
    BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==============================================================================
-- COMENTARIOS EN TABLAS Y COLUMNAS PARA DOCUMENTACIÓN
-- ==============================================================================

COMMENT ON TABLE users IS 'Usuarios registrados en el sistema';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt de la contraseña del usuario';
COMMENT ON COLUMN users.is_active IS 'Flag para soft delete - usuarios inactivos no pueden iniciar sesión';

COMMENT ON TABLE roles IS 'Roles de autorización del sistema (USER, ADMIN, etc.)';
COMMENT ON TABLE user_roles IS 'Tabla de unión para relación N:M entre usuarios y roles';

COMMENT ON TABLE addresses IS 'Direcciones de facturación y envío de los usuarios';
COMMENT ON COLUMN addresses.is_default IS 'Dirección por defecto del usuario para checkout rápido';

COMMENT ON TABLE categories IS 'Categorías de productos con soporte para jerarquías';
COMMENT ON COLUMN categories.parent_id IS 'Referencia a categoría padre para crear subcategorías';

COMMENT ON TABLE products IS 'Catálogo de productos disponibles para la venta';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - código único de inventario';
COMMENT ON COLUMN products.stock_quantity IS 'Cantidad disponible en inventario';

COMMENT ON TABLE reviews IS 'Reseñas y calificaciones de productos por usuarios';
COMMENT ON COLUMN reviews.is_verified_purchase IS 'Indica si la reseña es de un usuario que compró el producto';

COMMENT ON TABLE carts IS 'Carritos de compras persistentes por usuario';
COMMENT ON TABLE cart_items IS 'Items individuales dentro de cada carrito';

COMMENT ON TABLE orders IS 'Órdenes de compra procesadas (usuarios registrados y guests)';
COMMENT ON COLUMN orders.total_amount IS 'Total desnormalizado para optimizar consultas';
COMMENT ON COLUMN orders.status IS 'Estado actual: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED';

COMMENT ON TABLE order_items IS 'Items de órdenes con snapshot de información del producto';
COMMENT ON COLUMN order_items.price_at_purchase IS 'Precio congelado al momento de la compra';

COMMENT ON TABLE payments IS 'Registros de pagos procesados por gateways externos';
COMMENT ON COLUMN payments.gateway_response IS 'Respuesta completa del gateway en formato JSON';