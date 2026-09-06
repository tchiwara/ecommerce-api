CREATE TABLE carts (
                       id         BINARY(16)   DEFAULT (UUID_TO_BIN(UUID(), 1)) NOT NULL PRIMARY KEY,
                       user_id    BIGINT       NULL,
                       status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT uq_cart_user UNIQUE (user_id),
                       CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
                            id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            cart_id    BINARY(16)   NOT NULL,
                            product_id BIGINT       NOT NULL,
                            quantity   INT          NOT NULL,
                            unit_price DECIMAL(10,2) NOT NULL,
                            created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                            CONSTRAINT fk_item_cart
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_item_product
                                FOREIGN KEY (product_id)
                                    REFERENCES products(id),

                            CONSTRAINT uq_cart_product
                                UNIQUE (cart_id, product_id),

                            CONSTRAINT chk_cart_item_quantity
                                CHECK (quantity > 0)
);