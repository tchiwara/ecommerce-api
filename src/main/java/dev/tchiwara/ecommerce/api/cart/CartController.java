package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.cart.dtos.AddItemRequestDTO;
import dev.tchiwara.ecommerce.api.cart.dtos.CartResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddItemRequestDTO request
    ) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

}