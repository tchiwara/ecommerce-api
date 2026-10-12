package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
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
    private final OrderMapper orderMapper;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(Authentication authentication) {
        Order order = checkoutService.checkout(currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @GetMapping
    public PagedModel<OrderSummaryResponseDTO> list(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(orderService.listOrders(currentUserId(authentication), pageable));
    }

    @GetMapping("/{id}")
    public OrderResponseDTO get(Authentication authentication, @PathVariable Long id) {
        return orderService.getOrder(currentUserId(authentication), id);
    }

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
