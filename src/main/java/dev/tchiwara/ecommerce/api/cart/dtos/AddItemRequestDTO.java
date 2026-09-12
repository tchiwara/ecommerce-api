package dev.tchiwara.ecommerce.api.cart.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequestDTO {

    @NotNull(message = "Product Id cannot be null")
    private Long productId;
}
