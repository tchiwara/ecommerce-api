package dev.tchiwara.ecommerce.api.product.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockAdjustmentRequestDTO {

    @NotNull
    private Integer delta;
}