-- V2__Sample_Data.sql
-- Datos iniciales para desarrollo y testing (VERSIÓN ROBUSTA Y CORREGIDA)

-- ==============================================================================
-- ROLES BÁSICOS DEL SISTEMA
-- ==============================================================================
INSERT INTO roles (name, description, created_by) VALUES
                                                      ('ROLE_USER', 'Usuario estándar con permisos básicos', 'SYSTEM'),
                                                      ('ROLE_ADMIN', 'Administrador con acceso completo al sistema', 'SYSTEM'),
                                                      ('ROLE_MODERATOR', 'Moderador con permisos intermedios', 'SYSTEM');

-- ==============================================================================
-- USUARIOS DE PRUEBA
-- Contraseña para todos: "password123" (hasheada con BCrypt)
-- ==============================================================================
INSERT INTO users (first_name, last_name, email, password_hash, created_by) VALUES
                                                                                ('Juan', 'Pérez', 'admin@ecommerce.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SYSTEM'),
                                                                                ('María', 'González', 'user@ecommerce.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SYSTEM'),
                                                                                ('Carlos', 'Rodríguez', 'moderator@ecommerce.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SYSTEM'),
                                                                                ('Ana', 'López', 'customer@ecommerce.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SYSTEM');

-- ==============================================================================
-- ASIGNACIÓN DE ROLES A USUARIOS (CON SUBQUERIES)
-- ==============================================================================
INSERT INTO user_roles (user_id, role_id, assigned_by) VALUES
                                                           ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), 'SYSTEM'),
                                                           ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_USER'), 'SYSTEM'),
                                                           ((SELECT id FROM users WHERE email = 'user@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_USER'), 'SYSTEM'),
                                                           ((SELECT id FROM users WHERE email = 'moderator@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_MODERATOR'), 'SYSTEM'),
                                                           ((SELECT id FROM users WHERE email = 'moderator@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_USER'), 'SYSTEM'),
                                                           ((SELECT id FROM users WHERE email = 'customer@ecommerce.com'), (SELECT id FROM roles WHERE name = 'ROLE_USER'), 'SYSTEM');

-- ==============================================================================
-- DIRECCIONES DE PRUEBA (CON SUBQUERIES)
-- ==============================================================================
INSERT INTO addresses (user_id, street, city, state, postal_code, country, is_default, created_by) VALUES
                                                                                                       ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 'Av. Libertador 1234', 'Lima', 'Lima', '15001', 'Peru', true, 'SYSTEM'),
                                                                                                       ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 'Jr. Cusco 567', 'Lima', 'Lima', '15002', 'Peru', false, 'SYSTEM'),
                                                                                                       ((SELECT id FROM users WHERE email = 'user@ecommerce.com'), 'Calle Los Olivos 890', 'Arequipa', 'Arequipa', '04001', 'Peru', true, 'SYSTEM'),
                                                                                                       ((SELECT id FROM users WHERE email = 'customer@ecommerce.com'), 'Av. El Sol 321', 'Cusco', 'Cusco', '08001', 'Peru', true, 'SYSTEM');

-- ==============================================================================
-- CATEGORÍAS DE PRODUCTOS
-- ==============================================================================
INSERT INTO categories (name, description, display_order, created_by) VALUES
                                                                          ('Electrónicos', 'Dispositivos y gadgets electrónicos', 1, 'SYSTEM'),
                                                                          ('Ropa y Accesorios', 'Prendas de vestir y accesorios de moda', 2, 'SYSTEM'),
                                                                          ('Hogar y Jardín', 'Productos para el hogar y jardinería', 3, 'SYSTEM'),
                                                                          ('Deportes y Fitness', 'Equipamiento deportivo y fitness', 4, 'SYSTEM'),
                                                                          ('Libros y Medios', 'Libros, música y entretenimiento', 5, 'SYSTEM');

-- Subcategorías (CON SUBQUERIES para parent_id)
INSERT INTO categories (name, description, parent_id, display_order, created_by) VALUES
                                                                                     ('Smartphones', 'Teléfonos inteligentes y accesorios', (SELECT id FROM categories WHERE name = 'Electrónicos'), 1, 'SYSTEM'),
                                                                                     ('Laptops', 'Computadoras portátiles', (SELECT id FROM categories WHERE name = 'Electrónicos'), 2, 'SYSTEM'),
                                                                                     ('Audífonos', 'Audífonos y auriculares', (SELECT id FROM categories WHERE name = 'Electrónicos'), 3, 'SYSTEM'),
                                                                                     ('Ropa Hombre', 'Ropa masculina', (SELECT id FROM categories WHERE name = 'Ropa y Accesorios'), 1, 'SYSTEM'),
                                                                                     ('Ropa Mujer', 'Ropa femenina', (SELECT id FROM categories WHERE name = 'Ropa y Accesorios'), 2, 'SYSTEM'),
                                                                                     ('Calzado', 'Zapatos y zapatillas', (SELECT id FROM categories WHERE name = 'Ropa y Accesorios'), 3, 'SYSTEM');

-- ==============================================================================
-- PRODUCTOS DE PRUEBA (CON SUBQUERIES para category_id)
-- ==============================================================================
INSERT INTO products (name, description, price, sku, stock_quantity, category_id, image_url, brand, weight, dimensions, created_by) VALUES
                                                                                                                                        ('iPhone 15 Pro', 'Smartphone Apple con chip A17 Pro y cámara de 48MP', 1299.99, 'IPHONE-15-PRO-128', 25, (SELECT id FROM categories WHERE name = 'Smartphones'), 'https://example.com/iphone15pro.jpg', 'Apple', 187.0, '14.67x7.81x0.83 cm', 'SYSTEM'),
                                                                                                                                        ('Samsung Galaxy S24', 'Smartphone Android con pantalla Dynamic AMOLED 2X', 899.99, 'SAMSUNG-S24-256', 30, (SELECT id FROM categories WHERE name = 'Smartphones'), 'https://example.com/galaxys24.jpg', 'Samsung', 167.0, '14.7x7.06x0.76 cm', 'SYSTEM'),
                                                                                                                                        ('Google Pixel 8', 'Smartphone con IA avanzada y cámara computacional', 699.99, 'PIXEL-8-128', 20, (SELECT id FROM categories WHERE name = 'Smartphones'), 'https://example.com/pixel8.jpg', 'Google', 187.0, '15.04x7.06x0.89 cm', 'SYSTEM'),
                                                                                                                                        ('MacBook Air M3', 'Laptop ultradelgada con chip M3 y pantalla Retina', 1199.99, 'MACBOOK-AIR-M3-256', 15, (SELECT id FROM categories WHERE name = 'Laptops'), 'https://example.com/macbookair.jpg', 'Apple', 1240.0, '30.41x21.24x1.13 cm', 'SYSTEM'),
                                                                                                                                        ('Dell XPS 13', 'Laptop premium con procesador Intel Core i7', 999.99, 'DELL-XPS13-512', 12, (SELECT id FROM categories WHERE name = 'Laptops'), 'https://example.com/dellxps13.jpg', 'Dell', 1230.0, '29.57x19.91x1.49 cm', 'SYSTEM'),
                                                                                                                                        ('Lenovo ThinkPad X1', 'Laptop empresarial con certificación militar', 1399.99, 'THINKPAD-X1-1TB', 8, (SELECT id FROM categories WHERE name = 'Laptops'), 'https://example.com/thinkpadx1.jpg', 'Lenovo', 1355.0, '31.57x21.71x1.49 cm', 'SYSTEM'),
                                                                                                                                        ('AirPods Pro 2', 'Auriculares inalámbricos con cancelación de ruido', 249.99, 'AIRPODS-PRO2', 50, (SELECT id FROM categories WHERE name = 'Audífonos'), 'https://example.com/airpodspro.jpg', 'Apple', 50.8, '6.11x4.5x2.17 cm', 'SYSTEM'),
                                                                                                                                        ('Sony WH-1000XM5', 'Audífonos over-ear con cancelación de ruido líder', 399.99, 'SONY-WH1000XM5', 25, (SELECT id FROM categories WHERE name = 'Audífonos'), 'https://example.com/sonyxm5.jpg', 'Sony', 254.0, '26.94x19.6x8.0 cm', 'SYSTEM'),
                                                                                                                                        ('Bose QuietComfort', 'Audífonos premium con comodidad superior', 329.99, 'BOSE-QC45', 20, (SELECT id FROM categories WHERE name = 'Audífonos'), 'https://example.com/boseqc.jpg', 'Bose', 238.0, '18.5x15.24x7.62 cm', 'SYSTEM'),
                                                                                                                                        ('Polo Clásico', 'Polo de algodón 100% con fit regular', 29.99, 'POLO-M-NAVY-L', 100, (SELECT id FROM categories WHERE name = 'Ropa Hombre'), 'https://example.com/polo-navy.jpg', 'Generic', 200.0, 'L', 'SYSTEM'),
                                                                                                                                        ('Jean Slim Fit', 'Jeans de mezclilla con corte moderno', 79.99, 'JEAN-M-BLUE-32', 75, (SELECT id FROM categories WHERE name = 'Ropa Hombre'), 'https://example.com/jean-blue.jpg', 'Levi''s', 650.0, '32x32', 'SYSTEM'),
                                                                                                                                        ('Chaqueta de Cuero', 'Chaqueta de cuero genuino estilo motociclista', 199.99, 'JACKET-LEATHER-M', 15, (SELECT id FROM categories WHERE name = 'Ropa Hombre'), 'https://example.com/jacket-leather.jpg', 'Zara', 800.0, 'M', 'SYSTEM'),
                                                                                                                                        ('Vestido Floral', 'Vestido de verano con estampado floral', 59.99, 'DRESS-FLORAL-S', 40, (SELECT id FROM categories WHERE name = 'Ropa Mujer'), 'https://example.com/dress-floral.jpg', 'H&M', 300.0, 'S', 'SYSTEM'),
                                                                                                                                        ('Blusa de Seda', 'Blusa elegante de seda natural', 89.99, 'BLOUSE-SILK-M', 25, (SELECT id FROM categories WHERE name = 'Ropa Mujer'), 'https://example.com/blouse-silk.jpg', 'Zara', 150.0, 'M', 'SYSTEM'),
                                                                                                                                        ('Pantalón Palazzo', 'Pantalón amplio de tela fluida', 49.99, 'PANTS-PALAZZO-L', 30, (SELECT id FROM categories WHERE name = 'Ropa Mujer'), 'https://example.com/pants-palazzo.jpg', 'Mango', 400.0, 'L', 'SYSTEM'),
                                                                                                                                        ('Zapatillas Running', 'Zapatillas deportivas para correr con amortiguación', 129.99, 'SHOES-RUN-42', 60, (SELECT id FROM categories WHERE name = 'Calzado'), 'https://example.com/running-shoes.jpg', 'Nike', 280.0, '42 EU', 'SYSTEM'),
                                                                                                                                        ('Botas de Cuero', 'Botas casuales de cuero para invierno', 159.99, 'BOOTS-LEATHER-41', 35, (SELECT id FROM categories WHERE name = 'Calzado'), 'https://example.com/leather-boots.jpg', 'Timberland', 750.0, '41 EU', 'SYSTEM'),
                                                                                                                                        ('Sandalias Verano', 'Sandalias cómodas para la playa', 39.99, 'SANDALS-BEACH-38', 80, (SELECT id FROM categories WHERE name = 'Calzado'), 'https://example.com/beach-sandals.jpg', 'Havaianas', 180.0, '38 EU', 'SYSTEM'),
                                                                                                                                        ('Sofá Modular', 'Sofá de 3 plazas con tapicería de tela', 799.99, 'SOFA-3SEAT-GRAY', 5, (SELECT id FROM categories WHERE name = 'Hogar y Jardín'), 'https://example.com/sofa-gray.jpg', 'IKEA', 45000.0, '230x88x85 cm', 'SYSTEM'),
                                                                                                                                        ('Mesa de Centro', 'Mesa de centro de madera maciza', 199.99, 'TABLE-COFFEE-OAK', 12, (SELECT id FROM categories WHERE name = 'Hogar y Jardín'), 'https://example.com/coffee-table.jpg', 'West Elm', 15000.0, '120x60x45 cm', 'SYSTEM'),
                                                                                                                                        ('Set de Jardinería', 'Kit completo de herramientas para jardín', 89.99, 'GARDEN-KIT-DELUXE', 25, (SELECT id FROM categories WHERE name = 'Hogar y Jardín'), 'https://example.com/garden-kit.jpg', 'Gardena', 2500.0, '50x30x15 cm', 'SYSTEM'),
                                                                                                                                        ('Bicicleta Mountain', 'Bicicleta de montaña con 21 velocidades', 599.99, 'BIKE-MTN-26', 8, (SELECT id FROM categories WHERE name = 'Deportes y Fitness'), 'https://example.com/mountain-bike.jpg', 'Trek', 13500.0, '185x110x68 cm', 'SYSTEM'),
                                                                                                                                        ('Pesas Ajustables', 'Set de mancuernas ajustables de 5-50 lb', 299.99, 'WEIGHTS-ADJ-SET', 15, (SELECT id FROM categories WHERE name = 'Deportes y Fitness'), 'https://example.com/adjustable-weights.jpg', 'Bowflex', 11000.0, '43x20x20 cm', 'SYSTEM'),
                                                                                                                                        ('Esterilla Yoga', 'Mat antideslizante para yoga y pilates', 29.99, 'MAT-YOGA-BLUE', 50, (SELECT id FROM categories WHERE name = 'Deportes y Fitness'), 'https://example.com/yoga-mat.jpg', 'Manduka', 900.0, '183x61x0.5 cm', 'SYSTEM'),
                                                                                                                                        ('El Quijote - Edición Deluxe', 'Edición especial ilustrada de la obra maestra', 49.99, 'BOOK-QUIJOTE-DELUXE', 30, (SELECT id FROM categories WHERE name = 'Libros y Medios'), 'https://example.com/quijote-book.jpg', 'Planeta', 800.0, '24x17x4 cm', 'SYSTEM'),
                                                                                                                                        ('Curso de Programación', 'Libro completo de desarrollo con Spring Boot', 79.99, 'BOOK-SPRING-BOOT', 20, (SELECT id FROM categories WHERE name = 'Libros y Medios'), 'https://example.com/spring-book.jpg', 'O''Reilly', 600.0, '23x18x3 cm', 'SYSTEM'),
                                                                                                                                        ('Auriculares Gaming', 'Headset profesional para gaming con micrófono', 149.99, 'HEADSET-GAMING-PRO', 40, (SELECT id FROM categories WHERE name = 'Libros y Medios'), 'https://example.com/gaming-headset.jpg', 'Razer', 350.0, '19x9x20 cm', 'SYSTEM');

-- ==============================================================================
-- ÓRDENES DE EJEMPLO
-- ==============================================================================
INSERT INTO orders (user_id, total_amount, status, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, notes, created_by) VALUES
                                                                                                                                                                  ((SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 1679.97, 'DELIVERED', 'Av. Libertador 1234', 'Lima', 'Lima', '15001', 'Peru', 'Entrega en horario de oficina', 'SYSTEM'),
                                                                                                                                                                  ((SELECT id FROM users WHERE email = 'user@ecommerce.com'), 959.97, 'SHIPPED', 'Calle Los Olivos 890', 'Arequipa', 'Arequipa', '04001', 'Peru', 'Llamar antes de la entrega', 'SYSTEM');

-- Orden de invitado (CON DATOS DE INVITADO INCLUIDOS EN EL INSERT)
INSERT INTO orders (user_id, guest_email, guest_first_name, guest_last_name, total_amount, status, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, notes, created_by) VALUES
    (NULL, 'guest@email.com', 'Pedro', 'Silva', 289.98, 'PENDING', 'Jr. Lima 456', 'Trujillo', 'La Libertad', '13001', 'Peru', 'Pago contra entrega', 'SYSTEM');

-- ==============================================================================
-- ITEMS DE ÓRDENES (con precios históricos)
-- ==============================================================================
INSERT INTO order_items (order_id, product_id, product_name, product_sku, price_at_purchase, quantity, subtotal) VALUES
-- Orden de 'admin@ecommerce.com'
((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com')), (SELECT id FROM products WHERE sku = 'IPHONE-15-PRO-128'), 'iPhone 15 Pro', 'IPHONE-15-PRO-128', 1299.99, 1, 1299.99),
((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com')), (SELECT id FROM products WHERE sku = 'AIRPODS-PRO2'), 'AirPods Pro 2', 'AIRPODS-PRO2', 249.99, 1, 249.99),
((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com')), (SELECT id FROM products WHERE sku = 'SHOES-RUN-42'), 'Zapatillas Running', 'SHOES-RUN-42', 129.99, 1, 129.99),

-- Orden de 'user@ecommerce.com'
((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'user@ecommerce.com')), (SELECT id FROM products WHERE sku = 'SAMSUNG-S24-256'), 'Samsung Galaxy S24', 'SAMSUNG-S24-256', 899.99, 1, 899.99),
((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'user@ecommerce.com')), (SELECT id FROM products WHERE sku = 'POLO-M-NAVY-L'), 'Polo Clásico', 'POLO-M-NAVY-L', 29.99, 2, 59.98),

-- Orden de 'guest@email.com'
((SELECT id FROM orders WHERE guest_email = 'guest@email.com'), (SELECT id FROM products WHERE sku = 'DRESS-FLORAL-S'), 'Vestido Floral', 'DRESS-FLORAL-S', 59.99, 1, 59.99),
((SELECT id FROM orders WHERE guest_email = 'guest@email.com'), (SELECT id FROM products WHERE sku = 'SANDALS-BEACH-38'), 'Sandalias Verano', 'SANDALS-BEACH-38', 39.99, 1, 39.99),
((SELECT id FROM orders WHERE guest_email = 'guest@email.com'), (SELECT id FROM products WHERE sku = 'MAT-YOGA-BLUE'), 'Esterilla Yoga', 'MAT-YOGA-BLUE', 29.99, 1, 29.99),
((SELECT id FROM orders WHERE guest_email = 'guest@email.com'), (SELECT id FROM products WHERE sku = 'TABLE-COFFEE-OAK'), 'Mesa de Centro', 'TABLE-COFFEE-OAK', 159.99, 1, 159.99);

-- ==============================================================================
-- PAGOS ASOCIADOS A LAS ÓRDENES
-- ==============================================================================
INSERT INTO payments (order_id, payment_method, payment_gateway, transaction_id, amount, currency, status, processed_date, created_by) VALUES
                                                                                                                                           ((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com')), 'CREDIT_CARD', 'stripe', 'pi_1234567890abcdef', 1679.97, 'USD', 'SUCCESS', '2024-01-15 10:30:00', 'SYSTEM'),
                                                                                                                                           ((SELECT id FROM orders WHERE user_id = (SELECT id FROM users WHERE email = 'user@ecommerce.com')), 'PAYPAL', 'paypal', 'PAY-1AB2C3D4E5F6G7H8', 959.97, 'USD', 'SUCCESS', '2024-01-20 14:22:00', 'SYSTEM'),
                                                                                                                                           ((SELECT id FROM orders WHERE guest_email = 'guest@email.com'), 'CASH_ON_DELIVERY', 'manual', NULL, 289.98, 'USD', 'PENDING', NULL, 'SYSTEM');

-- ==============================================================================
-- RESEÑAS DE PRODUCTOS
-- ==============================================================================
INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, created_by) VALUES
                                                                                                        ((SELECT id FROM products WHERE sku = 'IPHONE-15-PRO-128'), (SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 5, 'Excelente producto', 'El iPhone 15 Pro supera mis expectativas. La cámara es increíble y el rendimiento es muy fluido.', true, 'SYSTEM'),
                                                                                                        ((SELECT id FROM products WHERE sku = 'IPHONE-15-PRO-128'), (SELECT id FROM users WHERE email = 'user@ecommerce.com'), 4, 'Muy bueno pero caro', 'Gran calidad como siempre Apple, pero el precio es elevado. Vale la pena si tienes el presupuesto.', false, 'SYSTEM'),
                                                                                                        ((SELECT id FROM products WHERE sku = 'SAMSUNG-S24-256'), (SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 4, 'Samsung sigue mejorando', 'El Galaxy S24 tiene una pantalla hermosa y buena duración de batería. La cámara nocturna podría mejorar.', true, 'SYSTEM'),
                                                                                                        ((SELECT id FROM products WHERE sku = 'AIRPODS-PRO2'), (SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 5, 'Los mejores auriculares', 'Los AirPods Pro 2 tienen una cancelación de ruido excepcional. Perfectos para trabajar desde casa.', true, 'SYSTEM'),
                                                                                                        ((SELECT id FROM products WHERE sku = 'POLO-M-NAVY-L'), (SELECT id FROM users WHERE email = 'user@ecommerce.com'), 3, 'Calidad regular', 'El polo está bien por el precio, pero la tela se ve un poco delgada. Cumple su función básica.', false, 'SYSTEM'),
                                                                                                        ((SELECT id FROM products WHERE sku = 'SHOES-RUN-42'), (SELECT id FROM users WHERE email = 'admin@ecommerce.com'), 5, 'Perfectas para correr', 'Estas zapatillas Nike son muy cómodas y tienen excelente amortiguación. Las recomiendo 100%.', true, 'SYSTEM');

-- ==============================================================================
-- ACTUALIZAR FECHAS PARA SIMULAR HISTORIALES REALISTAS (DE FORMA SEGURA)
-- ==============================================================================
UPDATE orders SET
                  order_date = '2024-01-15 09:45:00',
                  shipped_date = '2024-01-16 08:00:00',
                  delivered_date = '2024-01-18 15:30:00'
WHERE user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com');

UPDATE orders SET
                  order_date = '2024-01-20 11:20:00',
                  shipped_date = '2024-01-22 10:15:00'
WHERE user_id = (SELECT id FROM users WHERE email = 'user@ecommerce.com');

UPDATE orders SET
    order_date = '2024-01-25 16:45:00'
WHERE guest_email = 'guest@email.com';

-- Actualizar fechas de reseñas
UPDATE reviews SET created_at = '2024-01-19 20:30:00'
WHERE product_id = (SELECT id FROM products WHERE sku = 'IPHONE-15-PRO-128') AND user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com');

UPDATE reviews SET created_at = '2024-01-21 14:15:00'
WHERE product_id = (SELECT id FROM products WHERE sku = 'IPHONE-15-PRO-128') AND user_id = (SELECT id FROM users WHERE email = 'user@ecommerce.com');

UPDATE reviews SET created_at = '2024-01-23 09:45:00'
WHERE product_id = (SELECT id FROM products WHERE sku = 'SAMSUNG-S24-256') AND user_id = (SELECT id FROM users WHERE email = 'admin@ecommerce.com');


-- ==============================================================================
-- VISTAS ÚTILES PARA REPORTES Y CONSULTAS (NO REQUIEREN CAMBIOS)
-- ==============================================================================
CREATE VIEW product_summary AS
SELECT
    p.id,
    p.name,
    p.sku,
    p.price,
    p.stock_quantity,
    c.name as category_name,
    p.brand,
    COALESCE(AVG(r.rating), 0) as avg_rating,
    COUNT(r.id) as total_reviews,
    CASE
        WHEN p.stock_quantity > 20 THEN 'In Stock'
        WHEN p.stock_quantity > 0 THEN 'Low Stock'
        ELSE 'Out of Stock'
        END as stock_status
FROM products p
         LEFT JOIN categories c ON p.category_id = c.id
         LEFT JOIN reviews r ON p.id = r.product_id
WHERE p.is_active = true
GROUP BY p.id, p.name, p.sku, p.price, p.stock_quantity, c.name, p.brand;

CREATE VIEW user_statistics AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    COUNT(DISTINCT o.id) as total_orders,
    COALESCE(SUM(o.total_amount), 0) as total_spent,
    COUNT(DISTINCT r.id) as total_reviews,
    u.created_at as registration_date
FROM users u
         LEFT JOIN orders o ON u.id = o.user_id
         LEFT JOIN reviews r ON u.id = r.user_id
WHERE u.is_active = true
GROUP BY u.id, u.first_name, u.last_name, u.email, u.created_at;

CREATE VIEW order_summary AS
SELECT
    o.id,
    COALESCE(u.first_name || ' ' || u.last_name, o.guest_first_name || ' ' || o.guest_last_name) as customer_name,
    COALESCE(u.email, o.guest_email) as customer_email,
    o.total_amount,
    o.status,
    o.order_date,
    o.shipped_date,
    o.delivered_date,
    p.status as payment_status,
    COUNT(oi.id) as total_items
FROM orders o
         LEFT JOIN users u ON o.user_id = u.id
         LEFT JOIN payments p ON o.id = p.order_id
         LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, customer_name, customer_email, o.total_amount, o.status,
         o.order_date, o.shipped_date, o.delivered_date, p.status;

COMMENT ON VIEW product_summary IS 'Vista consolidada con estadísticas de productos para dashboards';
COMMENT ON VIEW user_statistics IS 'Estadísticas agregadas de usuarios para análisis de comportamiento';
COMMENT ON VIEW order_summary IS 'Resumen ejecutivo de órdenes para reportes gerenciales';