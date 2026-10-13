package dev.tchiwara.ecommerce.api.payment.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResponseDTO {

    private Long orderId;
    private String checkoutUrl;
}