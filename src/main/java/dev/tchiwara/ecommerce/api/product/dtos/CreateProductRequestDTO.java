package dev.tchiwara.ecommerce.api.product.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateProductRequestDTO extends ProductRequestDTO {

    @NotNull
    @Min(0)
    private Integer stockQuantity;
}
