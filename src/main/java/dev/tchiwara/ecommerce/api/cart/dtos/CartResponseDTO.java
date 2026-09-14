package dev.tchiwara.ecommerce.api.cart.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private UUID cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal total;
}