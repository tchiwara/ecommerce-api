package dev.tchiwara.ecommerce.api.cart.dtos;


import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequestDTO {

    @Positive(message = "Quantity should be positive")
    private int quantity;
}
