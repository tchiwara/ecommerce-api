package dev.tchiwara.ecommerce.api.product.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required!")
    private String name;

    @NotNull(message = "Product price is required!")
    @DecimalMin(value="0.0",inclusive = false,message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Category Id of Product is required!")
    private Long categoryId;
}
