package dev.tchiwara.ecommerce.api.global;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() { super("Cart is empty"); }
}