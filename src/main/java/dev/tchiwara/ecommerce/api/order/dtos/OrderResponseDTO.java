package dev.tchiwara.ecommerce.api.order.dtos;

import dev.tchiwara.ecommerce.api.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponseDTO> items;
}
