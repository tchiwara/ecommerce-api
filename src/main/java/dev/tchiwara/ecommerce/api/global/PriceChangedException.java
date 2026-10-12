package dev.tchiwara.ecommerce.api.global;

import java.math.BigDecimal;

public class PriceChangedException extends RuntimeException {
    public PriceChangedException(String name, BigDecimal oldPrice, BigDecimal newPrice) {
        super("Price of %s changed from %s to %s".formatted(name, oldPrice, newPrice));
    }
}