package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.cart.dtos.AddItemRequestDTO;
import dev.tchiwara.ecommerce.api.cart.dtos.CartResponseDTO;
import dev.tchiwara.ecommerce.api.cart.dtos.UpdateQuantityRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddItemRequestDTO request
    ) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateQuantityRequestDTO request
    ) {
        return ResponseEntity.ok(cartService.updateQuantity(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

}