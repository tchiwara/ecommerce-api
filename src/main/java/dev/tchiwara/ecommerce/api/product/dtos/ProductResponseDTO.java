package dev.tchiwara.ecommerce.api.product.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class ProductResponseDTO {

    private final Long id;
    private final String name;
    private final BigDecimal price;
    private final Long categoryId;

}
