package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
import dev.tchiwara.ecommerce.api.payment.dtos.CheckoutSessionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionResponseDTO> checkout(
            Authentication authentication
    ) {
        CheckoutSessionResponseDTO response =
                checkoutService.checkout(
                        currentUserId(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public PagedModel<OrderSummaryResponseDTO> list(
            Authentication authentication,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return new PagedModel<>(
                orderService.listOrders(
                        currentUserId(authentication),
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public OrderResponseDTO get(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return orderService.getOrder(
                currentUserId(authentication),
                id
        );
    }

    @PostMapping("/{id}/cancel")
    public OrderResponseDTO cancel(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return orderService.cancelOrder(
                currentUserId(authentication),
                id
        );
    }

    @PostMapping("/{id}/payment-session")
    public CheckoutSessionResponseDTO createPaymentSession(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return orderService.createPaymentSession(
                currentUserId(authentication),
                id
        );
    }

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}