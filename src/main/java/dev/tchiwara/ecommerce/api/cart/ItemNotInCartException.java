package dev.tchiwara.ecommerce.api.cart;

public class ItemNotInCartException extends RuntimeException {
    public ItemNotInCartException(Long productId) {
        super("Product " + productId + " is not in this cart");
    }
}