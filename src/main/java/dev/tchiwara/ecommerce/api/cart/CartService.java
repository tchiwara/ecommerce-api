package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.cart.dtos.AddItemRequestDTO;
import dev.tchiwara.ecommerce.api.cart.dtos.CartResponseDTO;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.product.Product;
import dev.tchiwara.ecommerce.api.product.ProductRepository;
import dev.tchiwara.ecommerce.api.user.User;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartResponseDTO addItem(Long userId, AddItemRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.newCartFor(user));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + request.getProductId() + " not found"));

        cart.addCartItem(product);

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    public CartResponseDTO getCart(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        return cartRepository.findByUserId(userId)
                .map(cartMapper::toResponse)
                .orElseGet(this::emptyCartResponse);
    }

    private CartResponseDTO emptyCartResponse() {
        CartResponseDTO response = new CartResponseDTO();
        response.setItems(List.of());
        response.setTotal(BigDecimal.ZERO.setScale(2));
        return response;
    }
}