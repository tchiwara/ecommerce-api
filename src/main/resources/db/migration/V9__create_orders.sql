CREATE TABLE orders (
                        id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id      BIGINT NOT NULL,
                        status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        total_amount DECIMAL(12,2) NOT NULL,
                        created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
                        INDEX idx_orders_user (user_id)
);

CREATE TABLE order_items (
                             id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id     BIGINT NOT NULL,
                             product_id   BIGINT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             unit_price   DECIMAL(10,2) NOT NULL,
                             quantity     INT NOT NULL CHECK (quantity > 0),
                             CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
                             INDEX idx_order_items_order (order_id)
);