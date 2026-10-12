package dev.tchiwara.ecommerce.api.global;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName) {
        super("Insufficient stock for " + productName);
    }
}